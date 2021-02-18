package org.coodex.fd.webapp.pojo;

public class Base64DownloadResp {
    private String fileName;
    private String contentType;
    private String extName;
    private String base64String;
    private long size;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtName() {
        return extName;
    }

    public void setExtName(String extName) {
        this.extName = extName;
    }

    public String getBase64String() {
        return base64String;
    }

    public void setBase64String(String base64String) {
        this.base64String = base64String;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
