package com.unconv.spring.utils;

import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_LENGTH;
import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_PREFIX;

import org.apache.commons.lang3.RandomStringUtils;

/** Utility class for generating access tokens. */
public class AccessTokenGenerator {

    /** Private constructor to hide the implicit public one */
    private AccessTokenGenerator() {
        // Private constructor to hide the implicit public one.
    }

    /**
     * Generates a random access token with a prefix.
     *
     * @return a randomly generated access token prefixed with "Bearer "
     */
    public static String generateAccessToken() {
        String randomPart = RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH);
        return TOKEN_PREFIX + randomPart;
    }
}
