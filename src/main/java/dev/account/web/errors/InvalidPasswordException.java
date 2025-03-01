package dev.account.web.errors;

import dev.core.exception.ErrorConstants;
import dev.core.exception.ProblemDetailWithCause;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

/**
 * @author Nelson Tanko
 */
public class InvalidPasswordException extends ErrorResponseException {

    public InvalidPasswordException() {
        super(
                HttpStatus.BAD_REQUEST,
                ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                        .withStatus(HttpStatus.BAD_REQUEST.value())
                        .withType(ErrorConstants.INVALID_PASSWORD_TYPE)
                        .withTitle("Invalid password")
                        .build(),
                null
        );
    }
}
