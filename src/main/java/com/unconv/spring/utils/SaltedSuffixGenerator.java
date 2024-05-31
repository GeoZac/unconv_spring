package com.unconv.spring.utils;

import static com.unconv.spring.consts.SensorAuthConstants.SALT_LENGTH;

import java.security.SecureRandom;
import java.util.Base64;

public class SaltedSuffixGenerator {

    private SaltedSuffixGenerator() {}

    public static String generateSaltedSuffix() {
        byte[] saltBytes = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
}
