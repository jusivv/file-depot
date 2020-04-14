package org.vvodes.fd.webapp.pojo;

/**
 * 错误信息响应
 */
public class ErrorResponse {
    /**
     * 错误代码
     */
    private String code;
    /**
     * 错误信息
     */
    private String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ErrorResponse() {
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
