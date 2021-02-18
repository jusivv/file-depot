package org.coodex.fd.webapp.pojo;

public class Base64UploadReq {
    /**
     * 文件服务客户端Id
     */
    private String clientId;
    /**
     * 文件访问令牌
     */
    private String token;
    /**
     * 文件名，包括扩展名
     */
    private String fileName;
    /**
     * 文件Content-Type
     */
    private String contentType;
    /**
     * 文件Base64字符串
     */
    private String base64File;

    /**
     * 文件是否加密存储
     */
    private boolean encrypt;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

    public String getBase64File() {
        return base64File;
    }

    public void setBase64File(String base64File) {
        this.base64File = base64File;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }
}
