package com.unconv.spring.matchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.UnconvUser;
import java.util.Map;
import java.util.UUID;
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
                return unconvUser != null
                        && isValidId(unconvUser.getId())
                        && isValidUsername(unconvUser.getUsername())
                        && isValidEmail(unconvUser.getEmail())
                        && containsNoPassword(unconvUser.getPassword());
            }

            private boolean isValidId(UUID id) {
                return id != null && id.version() == 4;
            }

            private boolean isValidUsername(String username) {
                return username != null && !username.isEmpty() && !username.isBlank();
            }

            private boolean isValidEmail(String email) {
                return email != null && !email.isEmpty() && !email.isBlank();
            }

            private boolean containsNoPassword(String password) {
                return password == null;
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
