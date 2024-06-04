package com.unconv.spring.utils;

import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_LENGTH;
import static com.unconv.spring.consts.SensorAuthConstants.TOKEN_PREFIX;

import org.apache.commons.lang3.RandomStringUtils;

public class AccessTokenGenerator {

    public static String generateAccessToken() {
        String randomPart = RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH);
        return TOKEN_PREFIX + randomPart;
    }
}
