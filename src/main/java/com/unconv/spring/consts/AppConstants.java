package com.unconv.spring.consts;

public final class AppConstants {

    private AppConstants() {
        // Private constructor to hide the implicit public one
    }

    public static final String PROFILE_REL = "release";
    public static final String PROFILE_NOT_REL = "!" + PROFILE_REL;
    public static final String PROFILE_TEST = "test";
    public static final String PROFILE_NOT_TEST = "!" + PROFILE_TEST;

    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // Environmental Reading
    public static final String DEFAULT_ER_SORT_BY = "timestamp";
    public static final String DEFAULT_ER_SORT_DIRECTION = "desc";

    // Sensor System
    public static final String DEFAULT_SS_SORT_BY = "sensorName";
    public static final String DEFAULT_SS_SORT_DIRECTION = DEFAULT_SORT_DIRECTION;

    // SensorAuthToken
    public static final String ACCESS_TOKEN = "access_token";
}
