package org.coodex.fd.webapp.pojo;

/**
 * 错误信息响应
 */
public class ResponseMessage {
    /**
     * 错误代码
     */
    private String code;
    /**
     * 错误信息
     */
    private String message;

    public ResponseMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseMessage() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
