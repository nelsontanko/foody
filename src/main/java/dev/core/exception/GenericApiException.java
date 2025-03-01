package dev.core.exception;

/**
 * @author Nelson Tanko
 */

public class GenericApiException extends AbstractApiException {

    public GenericApiException() {
        super(ErrorCode.GENERAL_DEFAULT_ERROR_CODE);
    }
    public GenericApiException(String message) {
        super(message);
    }

    public GenericApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public GenericApiException(ErrorCode errorCode, Object[] args) {
        super(errorCode, args);
    }

    public GenericApiException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
