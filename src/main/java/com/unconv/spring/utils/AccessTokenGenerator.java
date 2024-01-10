package com.unconv.spring.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class AccessTokenGenerator {

    private static final String TOKEN_PREFIX = "UNCONV";
    private static final int TOKEN_LENGTH = 25 - TOKEN_PREFIX.length();

    public static String generateAccessToken() {
        String randomPart = RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH);
        return TOKEN_PREFIX + randomPart;
    }
}
