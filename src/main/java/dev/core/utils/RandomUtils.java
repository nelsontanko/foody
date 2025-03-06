package dev.core.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public final class RandomUtils {

    private static final int OTP_COUNT = 6;
    private static final int DEF_COUNT = 20;

    private static final SecureRandom SECURE_RANDOM;

    static {
        SECURE_RANDOM = new SecureRandom();
        SECURE_RANDOM.nextBytes(new byte[64]);
    }

    private RandomUtils() {
    }

    /**
     * Generate an activation key.
     *
     * @return the generated activation key.
     */
    public static String generateActivationKey() {
        return generateRandomNumericString();
    }

    /**
     * <p>generateRandomNumericString. for OTP</p>
     *
     * @return a {@link String} object.
     */
    public static String generateRandomNumericString() {
        return RandomStringUtils.random(OTP_COUNT, 0, 0, false, true, null, SECURE_RANDOM);
    }

    /**
     * <p>generateRandomAlphanumericString.</p>
     *
     * @return a {@link String} object.
     */
    public static String generateRandomAlphanumericString() {
        return RandomStringUtils.random(DEF_COUNT, 0, 0, true, true, null, SECURE_RANDOM);
    }

    /**
     * Generate a password.
     *
     * @return the generated password.
     */
    public static String generatePassword() {
        return generateRandomAlphanumericString();
    }

    /**
     * Generate a reset key.
     *
     * @return the generated reset key.
     */
    public static String generateResetKey() {
        return generateRandomAlphanumericString();
    }
}
