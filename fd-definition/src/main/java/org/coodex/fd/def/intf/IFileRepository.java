package org.coodex.fd.def.intf;

import org.coodex.fd.def.pojo.StoreFileInfo;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件仓库接口
 */
public interface IFileRepository {
    /**
     * 存储文件，同时存储文件信息
     * @param is        文件输入流
     * @param fileInfo  文件信息
     * @return          文件ID
     */
    String store(InputStream is, StoreFileInfo fileInfo);

    /**
     * 存储文件，不存储文件信息
     * @param is        文件输入流
     * @param owner     文件属主
     * @param extName   文件扩展名
     * @return          文件ID
     */
    String store(InputStream is, String owner, String extName);

    /**
     * 获取文件信息
     * @param fileId    文件ID
     * @return          文件信息
     */
    StoreFileInfo getFileInfo(String fileId);

    /**
     * 设置（存储）文件信息
     * @param fileId    文件ID
     * @param fileInfo  文件信息
     */
    void setFileInfo(String fileId, StoreFileInfo fileInfo);

    /**
     * 获取文件
     * @param os        文件输出流
     * @param fileId    文件ID
     */
    void fetch(OutputStream os, String fileId);

    /**
     * 获取文件
     * @param fileId    文件ID
     * @return          二进制文件数据
     */
    byte[] fetch(String fileId);

    /**
     * 删除文件
     * @param fileId    文件ID
     */
    void delete(String fileId);

}
