package org.aarboard.nextcloud.api.exception;

public class NextcloudApiException extends RuntimeException {
    private static final long serialVersionUID = 8088239559973590632L;

    private Integer statusCode;

    public NextcloudApiException(Throwable cause) {
        super(cause);
    }

    public NextcloudApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
