package org.vvodes.fd.def.util;

import org.vvodes.fd.def.intf.IStreamInterceptor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AspectOutputStream extends OutputStream {
    private OutputStream origin;
    private List<IStreamInterceptor> interceptors;

    public AspectOutputStream(OutputStream origin, IStreamInterceptor... interceptors) {
        this.origin = origin;
        this.interceptors = new ArrayList<IStreamInterceptor>();
        if (interceptors != null && interceptors.length > 0) {
            this.interceptors.addAll(Arrays.asList(interceptors));
        }
    }

    public AspectOutputStream(OutputStream origin) {
        this.origin = origin;
        this.interceptors = new ArrayList<IStreamInterceptor>();
    }

    @Override
    public void write(int b) throws IOException {
        int result = b;
        if (interceptors != null && b >= 0 && b <= 255) {
            for (IStreamInterceptor interceptor : interceptors) {
                result = interceptor.load(b, result);
            }
        }
        origin.write(result);
    }

    @Override
    public void flush() throws IOException {
        origin.flush();
    }

    @Override
    public void close() throws IOException {
        origin.close();
        if (interceptors != null) {
            for (IStreamInterceptor interceptor : interceptors) {
                if (!interceptor.isFinished()) {
                    interceptor.finish();
                }
            }
        }
    }
}
