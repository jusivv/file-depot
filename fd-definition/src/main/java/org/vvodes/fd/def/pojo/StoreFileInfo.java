package org.vvodes.fd.def.pojo;

public class StoreFileInfo extends CommonFileInfo {

    /**
     * 文件加密模式
     */
    private String cipherModel;
    /**
     * 加密密钥盐
     */
    private String salt;

    public String getCipherModel() {
        return cipherModel;
    }

    public void setCipherModel(String cipherModel) {
        this.cipherModel = cipherModel;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
