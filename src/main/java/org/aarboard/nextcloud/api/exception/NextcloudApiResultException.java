package org.aarboard.nextcloud.api.exception;

public class NextcloudApiResultException extends NextcloudApiException {
    private static final long serialVersionUID = 8088239559973590632L;

    private int statusCode;

    public NextcloudApiResultException(Throwable cause) {
        super(cause);
    }

    public NextcloudApiResultException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
