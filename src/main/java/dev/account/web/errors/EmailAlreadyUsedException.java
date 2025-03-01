package dev.account.web.errors;

import dev.core.exception.BadRequestAlertException;
import dev.core.exception.ErrorConstants;

import java.io.Serial;

/**
 * @author Nelson Tanko
 */

public class EmailAlreadyUsedException extends BadRequestAlertException {

    @Serial private static final long serialVersionUID = 1L;

    public EmailAlreadyUsedException() {
        super(ErrorConstants.EMAIL_ALREADY_USED_TYPE, "Email is already in use!", "userManagement", "email.exists");
    }
}
