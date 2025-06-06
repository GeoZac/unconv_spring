package com.unconv.spring.web.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.exception.SensorAuthTokenException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Component class responsible for handling exceptions related to sensor authentication. */
@Component
public class SensorAuthTokenExceptionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(SensorAuthTokenExceptionHandler.class);

    /**
     * Handles exceptions thrown during sensor authentication and writes appropriate error response
     * to the HttpServletResponse.
     *
     * @param response The {@link HttpServletResponse} object to which the error response will be
     *     written.
     * @param exception The {@link SensorAuthTokenException} that was thrown during sensor
     *     authentication.
     * @throws IOException If an I/O error occurs while writing the error response.
     */
    public void handleSensorAuthException(
            HttpServletResponse response, SensorAuthTokenException exception) throws IOException {
        // Log the exception message and token
        logger.warn("Sensor authentication failed. Token: {}", exception.getToken(), exception);

        Map<String, String> map = new HashMap<>();
        map.put("message", exception.getMessage());
        map.put("token", exception.getToken());
        map.put("timestamp", OffsetDateTime.now().toString());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(map));
    }
}
