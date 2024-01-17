package com.unconv.spring.matchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.UnconvUser;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class UnconvUserMatcher {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Matcher<Object> validUnconvUser() {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Object object) {
                UnconvUser unconvUser = convertToUnconvUser((Map<?, ?>) object);
                return unconvUser != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A valid UnconvUser with non-null and valid fields");
            }

            private UnconvUser convertToUnconvUser(Map<?, ?> map) {
                try {
                    return objectMapper.convertValue(map, UnconvUser.class);
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }
}
