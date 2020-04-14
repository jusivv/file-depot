package org.vvodes.fd.def.pojo;

/**
 * 通用文件信息
 */
public class CommonFileInfo {
    /**
     * 文件属主
     */
    private String owner;
    /**
     * 原始文件名
     */
    private String originName;
    /**
     * 文件扩展名
     */
    private String extName;
    /**
     * 文件ID，可用于获取文件
     */
    private String fileId;
    /**
     * 文件存储时间戳
     */
    private long storeTime;
    /**
     * 文件大小，字节
     */
    private long size;
    /**
     * 文件哈希值
     */
    private String hashValue;
    /**
     * 文件哈希算法
     */
    private String hashAlgorithm;
    /**
     * MIME类型
     */
    private String contentType;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getExtName() {
        return extName;
    }

    public void setExtName(String extName) {
        this.extName = extName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public long getStoreTime() {
        return storeTime;
    }

    public void setStoreTime(long storeTime) {
        this.storeTime = storeTime;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
