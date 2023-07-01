package com.unconv.spring.consts;

public final class MessageConstants {

    private MessageConstants() {
        // Placeholder constructor
    }

    // EnvironmentalReading
    public static final String ENVT_RECORD_ACCEPTED = "Record added successfully";
    public static final String ENVT_RECORD_REJ_USER = "User validation failed on SensorSystem";
    public static final String ENVT_RECORD_REJ_SENS = "Unknown SensorSystem on request";

    public static final String ENVT_FILE_REJ_ERR = "Could not upload the file: %s!";
    public static final String ENVT_FILE_FORMAT_ERROR = "Please upload a csv file!";

    public static final String ENVT_VALID_SENSOR_SYSTEM = "Sensor system cannot be empty";

    // Unconv user
    public static final String USER_NAME_IN_USE = "Username already taken";
    public static final String USER_CREATE_SUCCESS = "User created successfully";
}
