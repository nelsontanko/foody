package dev.core.validation;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import dev.core.exception.ErrorCode;
import dev.core.exception.GenericApiException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Nelson Tanko
 */
@Slf4j
public class PhoneNumberConstraintValidator implements ConstraintValidator<ValidMobileNumber, String> {

    private String region;

    @Override
    public void initialize(ValidMobileNumber constraintAnnotation) {
        this.region = constraintAnnotation.region();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        if (phoneNumber == null) {
            return true;
        } else if (phoneNumber.isEmpty()) {
            return false;
        }

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(phoneNumber, region);
            if (!phoneNumberUtil.isValidNumber(numberProto)) {
                throw new GenericApiException(ErrorCode.USER_PROFILE_INVALID_PHONE);
            }
            return phoneNumberUtil.isValidNumber(numberProto) &&
                    phoneNumberUtil.getNumberType(numberProto) == PhoneNumberUtil.PhoneNumberType.MOBILE;
        } catch (NumberParseException e) {
            log.error("Error while validating phone number. number={}, country={}", phoneNumber, region);
            return false;
        }
    }

    public static String normalize(String phoneNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(phoneNumber, "NG");
            if (phoneNumberUtil.isValidNumber(numberProto)) {
                return phoneNumberUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (NumberParseException e) {
            throw new GenericApiException();
        }
        return phoneNumber; // Return original if normalization fails
    }
}
