package com.unconv.spring.utils;

import static com.unconv.spring.consts.SensorAuthConstants.SALT_LENGTH;

import java.security.SecureRandom;
import java.util.Base64;

/** Utility class for generating salted suffixes. */
public class SaltedSuffixGenerator {

    private SaltedSuffixGenerator() {}

    /**
     * Generates a salted suffix using a secure random algorithm.
     *
     * @return a Base64-encoded string representing the generated salted suffix
     */
    public static String generateSaltedSuffix() {
        byte[] saltBytes = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
}
