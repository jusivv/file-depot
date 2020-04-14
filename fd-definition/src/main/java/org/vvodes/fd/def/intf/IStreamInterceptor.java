package org.vvodes.fd.def.intf;

/**
 * 流拦截器
 */
public interface IStreamInterceptor {

    /**
     * 加载流数据
     * @param originData    流中的原始数据
     * @param processData   经过上层拦截器处理后的过程数据
     * @return              当前拦截器处理后的数据
     */
    int load(int originData, int processData);

    /**
     * 流处理结束
     */
    void finish();

    /**
     * 是否处理完成
     * @return
     */
    boolean isFinished();
}
