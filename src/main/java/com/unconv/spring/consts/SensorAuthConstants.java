package com.unconv.spring.consts;

public class SensorAuthConstants {

    public static final String TOKEN_PREFIX = "UNCONV";
    public static final int TOKEN_LENGTH = 25 - TOKEN_PREFIX.length();

    public static final int SALT_LENGTH = 16;
    public static final int HASH_STRING_LEN = 24;

    private SensorAuthConstants() {
        // Placeholder constructor to prevent being instantiated
    }
}
