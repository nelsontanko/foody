package dev.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

import java.net.URI;

/**
 * @author Nelson Tanko
 */
@Getter @SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class BadRequestAlertException extends ErrorResponseException {

    private final String entityName;

    private final String errorKey;

    public BadRequestAlertException(String defaultMessage, String entityName, String errorKey) {
        this(ErrorConstants.DEFAULT_TYPE, defaultMessage, entityName, errorKey);
    }

    public BadRequestAlertException(URI type, String defaultMessage, String entityName, String errorKey) {
        super(
                HttpStatus.BAD_REQUEST,
                ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                        .withType(type)
                        .withTitle(defaultMessage)
                        .withProperty("message", "error." + errorKey)
                        .withProperty("params", entityName)
                        .build(),
                null
        );
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    public ProblemDetailWithCause getProblemDetailWithCause() {
        return (ProblemDetailWithCause) this.getBody();
    }

}
