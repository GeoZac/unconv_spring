package com.unconv.spring.exception;

import static com.unconv.spring.consts.MessageConstants.SENS_AUTH_EXPIRED;

import lombok.Getter;

@Getter
public class ExpiredAuthTokenException extends SensorAuthTokenException {

    public ExpiredAuthTokenException(String token) {
        super(SENS_AUTH_EXPIRED, token);
    }
}
