package com.unconv.spring.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SensorAuthTokenMatcher {

    public static Matcher<Object> validSensorAuthToken(boolean isMaskedSensorAuthToken) {
        return new TypeSafeMatcher<>() {

            @Override
            protected boolean matchesSafely(Object object) {
                String sensorAuthTokenString = (String) object;
                String maskedSensorAuthTokenPattern = "UNCONV[*]{19}[A-Za-z0-9+/]{22}==";
                String unmaskedSensorAuthTokenPattern = "UNCONV[A-Za-z0-9]{19}.*";

                String selectedPattern =
                        isMaskedSensorAuthToken
                                ? maskedSensorAuthTokenPattern
                                : unmaskedSensorAuthTokenPattern;

                return sensorAuthTokenString.matches(selectedPattern)
                        && sensorAuthTokenString.length() == 49;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("A valid SensorAuthToken matching ")
                        .appendText(
                                isMaskedSensorAuthToken ? "masked pattern" : "unmasked pattern");
            }
        };
    }
}
