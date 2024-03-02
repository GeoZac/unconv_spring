package com.unconv.spring.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class SaltedSuffixGenerator {

    private static final int SALT_LENGTH = 16;

    private SaltedSuffixGenerator() {}

    public static String generateSaltedSuffix() {
        byte[] saltBytes = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
}
