package org.vvodes.fd.def.util;

import org.vvodes.fd.def.intf.IStreamInterceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AspectInputStream extends InputStream {

    private InputStream origin;

    private List<IStreamInterceptor> interceptors;

    public AspectInputStream(InputStream origin, IStreamInterceptor... interceptors) {
        this.origin = origin;
        this.interceptors = new ArrayList<IStreamInterceptor>();
        if (interceptors != null && interceptors.length > 0) {
            this.interceptors.addAll(Arrays.asList(interceptors));
        }
    }

    public AspectInputStream(InputStream origin) {
        this.origin = origin;
        this.interceptors = new ArrayList<IStreamInterceptor>();
    }

    /**
     * 增加拦截器
     * @param interceptor   拦截器
     */
    public void addInterceptor(IStreamInterceptor interceptor) {
        if (interceptors != null && interceptor != null) {
            interceptors.add(interceptor);
        }
    }

    /**
     * 清除所有拦截器
     */
    public void clearInterceptor() {
        if (interceptors != null) {
            interceptors.clear();
        }
    }

    @Override
    public int read() throws IOException {
        int originData = origin.read();
        int processData = originData;
        if (originData >= 0 && interceptors != null) {
            for (IStreamInterceptor interceptor : interceptors) {
                processData = interceptor.load(originData, processData);
            }
        }
        return processData;
    }

    @Override
    public void close() throws IOException {
        origin.close();
        if (interceptors != null) {
            for (IStreamInterceptor interceptor : interceptors) {
                interceptor.finish();
            }
        }
    }
}
