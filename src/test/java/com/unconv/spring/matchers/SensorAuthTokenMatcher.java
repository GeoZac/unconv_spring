package com.unconv.spring.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SensorAuthTokenMatcher {

    public static Matcher<Object> validSensorAuthToken() {
        return new TypeSafeMatcher<>() {

            @Override
            protected boolean matchesSafely(Object object) {
                String sensorAuthTokenString = (String) object;
                String maskedSensorAuthTokenPattern = "UNCONV[A-Za-z0-9*]{19}.*";
                return sensorAuthTokenString.matches(maskedSensorAuthTokenPattern)
                        && sensorAuthTokenString.length() == 49;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A valid SensorAuthToken");
            }
        };
    }
}
