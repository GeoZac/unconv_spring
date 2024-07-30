package com.unconv.spring.consts;

public final class MessageConstants {

    private MessageConstants() {
        // Placeholder constructor
    }

    // EnvironmentalReading
    public static final String ENVT_RECORD_ACCEPTED = "Record added successfully";
    public static final String ENVT_RECORD_REJ_USER = "User validation failed on SensorSystem";
    public static final String ENVT_RECORD_REJ_SENS = "Unknown SensorSystem on request";
    public static final String ENVT_RECORD_REJ_DLTD = "Invalid Sensor system on request";
    public static final String ENVT_RECORD_REJ_INAT = "Inactive sensor system on request";

    public static final String SENS_RECORD_REJ_USER = "Unknown UnconvUser on request";

    public static final String ENVT_FILE_REJ_ERR = "Could not upload the file: %s!";
    public static final String ENVT_FILE_FORMAT_ERROR = "Please upload a csv file!";

    public static final String ENVT_VALID_SENSOR_SYSTEM = "Sensor system cannot be empty";

    // Unconv user
    public static final String USER_NAME_IN_USE = "Username already taken";
    public static final String USER_CREATE_SUCCESS = "User created successfully";

    public static final String USER_UPDATE_SUCCESS = "Updated Unconvuser info";
    public static final String USER_WRONG_PASSWORD = "Wrong password";
    public static final String USER_PROVIDE_PASSWORD = "Provide current password";

    // SensorAuthToken
    public static final String SENS_AUTH_TOKEN_GEN_SUCCESS = "Generated New Sensor Auth Token";

    // SensorAuthException message
    public static final String SENS_AUTH_SHORT = "Invalid token length";
    public static final String SENS_AUTH_EXPIRED = "Expired API token";
    public static final String SENS_AUTH_UNKNOWN = "Unknown API token";
    public static final String SENS_AUTH_MALFORMED = "Malformed API token";
}
