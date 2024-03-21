package com.unconv.spring.web.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.exception.MalformedAuthTokenException;
import com.unconv.spring.exception.UnknownAuthTokenException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class SensorAuthTokenExceptionHandler {

    public void handleUnknownAuthToken(
            HttpServletResponse response, UnknownAuthTokenException exception) throws IOException {

        Map<String, String> map = new HashMap<>();
        map.put("message", exception.getMessage());
        map.put("token", exception.getToken());
        map.put("timestamp", OffsetDateTime.now().toString());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(map));
    }

    public void handleMalformedAuthToken(
            HttpServletResponse response, MalformedAuthTokenException exception)
            throws IOException {

        Map<String, String> map = new HashMap<>();
        map.put("message", exception.getMessage());
        map.put("token", exception.getToken());
        map.put("timestamp", OffsetDateTime.now().toString());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(map));
    }
}
