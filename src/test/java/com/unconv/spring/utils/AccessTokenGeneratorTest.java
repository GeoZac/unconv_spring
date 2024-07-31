package com.unconv.spring.utils;

import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class AccessTokenGeneratorTest {

    @Test
    void testGenerateAccessToken() {
        String accessToken = AccessTokenGenerator.generateAccessToken();

        assertEquals(25, accessToken.length(), "The length of the generated token is incorrect.");

        assertTrue(
                accessToken.startsWith(TOKEN_PREFIX),
                "The generated token does not start with the correct prefix.");

        String randomPart = accessToken.substring(TOKEN_PREFIX.length());
        assertTrue(
                StringUtils.isAlphanumeric(randomPart),
                "The random part of the token is not alphanumeric.");
    }
}
