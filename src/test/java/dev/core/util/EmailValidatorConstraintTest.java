package dev.core.util;

import dev.core.validation.EmailConstraintValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class EmailValidatorConstraintTest {

    private EmailConstraintValidator emailValidator;

    @BeforeEach
    void setUp() {
        emailValidator = new EmailConstraintValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "valid.email@domain.co",
            "email@sub.domain.com",
            "first.last@company.org",
            "name+alias@gmail.com",
            "1234567890@example.net",
    })
    void shouldValidateCorrectEmails(String email) {
        assertThat(emailValidator.isValid(email, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@missinglocal.com",
            "missingdomain@.com",
            "missing@dotcom",
            "missing@domain",
            "double..dot@example.com",
            "name@domain..com",
            "invalid!char@example.com",
            "test@domain.c",
            "test@domain.abcdefg"
    })
    void shouldInvalidateIncorrectEmails(String email) {
        assertThat(emailValidator.isValid(email, null)).isFalse();
    }

    @Test
    void shouldInvalidateNullEmail() {
        assertThat(emailValidator.isValid(null, null)).isFalse();
    }

    @Test
    void shouldInvalidateEmptyEmail() {
        assertThat(emailValidator.isValid("", null)).isFalse();
    }

    @Test
    void shouldInvalidateEmailWithSpaces() {
        assertThat(emailValidator.isValid(" user @example.com ", null)).isFalse();
    }
}
