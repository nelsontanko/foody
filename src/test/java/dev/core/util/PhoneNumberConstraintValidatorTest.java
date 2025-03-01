package dev.core.util;

import dev.core.validation.PhoneNumberConstraintValidator;
import dev.core.validation.ValidMobileNumber;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PhoneNumberConstraintValidator.class})
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PhoneNumberConstraintValidatorTest {

    @Autowired
    private PhoneNumberConstraintValidator phoneNumberConstraintValidator;

    /**
     * Method under test: {@link PhoneNumberConstraintValidator#initialize(ValidMobileNumber)} (ValidPhoneNumber)}
     */
    @Test
    void testInitialize() {
        // Arrange
        ValidMobileNumber constraintAnnotation = mock(ValidMobileNumber.class);
        when(constraintAnnotation.region()).thenReturn("NG");

        // Act
        phoneNumberConstraintValidator.initialize(constraintAnnotation);

        // Assert
        verify(constraintAnnotation).region();
    }

    /**
     * Method under test: {@link PhoneNumberConstraintValidator#isValid(String, ConstraintValidatorContext)}
     */
    @Test
    void testIsValid() {
        // Arrange
        ClockProvider clockProvider = mock(ClockProvider.class);
        ConstraintValidatorContext context = new ConstraintValidatorContextImpl(
                clockProvider,
                PathImpl.createRootPath(),
                null,
                "Constraint Validator Payload",
                ExpressionLanguageFeatureLevel.DEFAULT,
                ExpressionLanguageFeatureLevel.DEFAULT
        );

        // Act and Assert
        assertTrue(phoneNumberConstraintValidator.isValid(null, context));
        assertFalse(phoneNumberConstraintValidator.isValid("", context));
    }

    @Test
    void testNormalize() {
        // Arrange, Act and Assert
        String normalizedNumber = PhoneNumberConstraintValidator.normalize("07080899922");
        assertEquals("+2347080899922", PhoneNumberConstraintValidator.normalize("7080899922"));
        assertEquals("+2347080899922", PhoneNumberConstraintValidator.normalize("07080899922"));
        assertThat(normalizedNumber).isNotEqualTo("+2337080899922");
    }
}
