package dev.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

/**
 * @author Nelson Tanko
 */
@Getter
abstract class AbstractApiException extends ErrorResponseException {
    private ErrorCode errorCode;
    private Object[] args;
    private String message;

    public AbstractApiException(String message) {
        super(
                HttpStatusCode.valueOf(500),
                ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                        .withTitle(message)
                        .build(),
                null
        );
        this.message = message;
    }

    public AbstractApiException(ErrorCode errorCode) {
        super(
                errorCode.getHttpStatus(),
                ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                        .withStatus(errorCode.getHttpStatus().value())
                        .withTitle(errorCode.getMessage())
                        .build(),
                null
        );
        this.errorCode = errorCode;
    }

    public AbstractApiException(ErrorCode errorCode, Object[] args) {
        super(
                errorCode.getHttpStatus(),
                ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                        .withStatus(errorCode.getHttpStatus().value())
                        .withTitle(errorCode.getMessage())
                        .withProperty("args", args)
                        .build(),
                null
        );
        this.errorCode = errorCode;
    }

    public AbstractApiException(ErrorCode errorCode, String message) {
        super(
                errorCode.getHttpStatus(),
                ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                        .withStatus(errorCode.getHttpStatus().value())
                        .withTitle(message)
                        .build(),
                null
        );
        this.errorCode = errorCode;
    }

    public ProblemDetailWithCause getProblemDetailWithCause() {
        return (ProblemDetailWithCause) this.getBody();
    }
}
