package dev.account.web.errors;

import dev.core.exception.BadRequestAlertException;
import dev.core.exception.ErrorConstants;

import java.io.Serial;

/**
 * @author Nelson Tanko
 */

public class MobileNumberAlreadyUsedException extends BadRequestAlertException {

    @Serial private static final long serialVersionUID = 1L;

    public MobileNumberAlreadyUsedException() {
        super(ErrorConstants.MOBILE_NUMBER_ALREADY_USED_TYPE, "Mobile number is already in use!", "userManagement", "mobile.number.exists");
    }
}
