package org.coodex.fd.webapp.util;

public class FileDepotWebException extends RuntimeException {
    private int statusCode;

    public FileDepotWebException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public FileDepotWebException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public FileDepotWebException(Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
