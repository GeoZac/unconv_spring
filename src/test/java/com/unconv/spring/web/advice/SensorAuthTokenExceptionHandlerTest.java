package com.unconv.spring.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.exception.ExpiredAuthTokenException;
import com.unconv.spring.exception.InvalidTokenLengthException;
import com.unconv.spring.exception.MalformedAuthTokenException;
import com.unconv.spring.exception.UnknownAuthTokenException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SensorAuthTokenExceptionHandlerTest {

    @Test
    void testHandleUnknownAuthToken() throws IOException {

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        SensorAuthTokenExceptionHandler handler = new SensorAuthTokenExceptionHandler();

        UnknownAuthTokenException exception = new UnknownAuthTokenException("Unknown token");

        handler.handleSensorAuthException(response, exception);

        String actualJson = stringWriter.toString();

        Map actualMap = new ObjectMapper().readValue(actualJson, Map.class);
        assertEquals(actualMap.get("message"), exception.getMessage());
        assertEquals(actualMap.get("token"), exception.getToken());
    }

    @Test
    void testHandleMalformedAuthToken() throws IOException {

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        SensorAuthTokenExceptionHandler handler = new SensorAuthTokenExceptionHandler();

        MalformedAuthTokenException exception = new MalformedAuthTokenException("Malformed token");

        handler.handleSensorAuthException(response, exception);

        String actualJson = stringWriter.toString();

        Map actualMap = new ObjectMapper().readValue(actualJson, Map.class);
        assertEquals(actualMap.get("message"), exception.getMessage());
        assertEquals(actualMap.get("token"), exception.getToken());
    }

    @Test
    void testHandleExpiredAuthToken() throws IOException {

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        SensorAuthTokenExceptionHandler handler = new SensorAuthTokenExceptionHandler();

        ExpiredAuthTokenException exception = new ExpiredAuthTokenException("Expired token");

        handler.handleSensorAuthException(response, exception);

        String actualJson = stringWriter.toString();

        Map actualMap = new ObjectMapper().readValue(actualJson, Map.class);
        assertEquals(actualMap.get("message"), exception.getMessage());
        assertEquals(actualMap.get("token"), exception.getToken());
    }

    @Test
    void testHandleTokenOfInvalidLength() throws IOException {

        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        SensorAuthTokenExceptionHandler handler = new SensorAuthTokenExceptionHandler();

        InvalidTokenLengthException exception =
                new InvalidTokenLengthException("Invalid token length");

        handler.handleSensorAuthException(response, exception);

        String actualJson = stringWriter.toString();

        Map actualMap = new ObjectMapper().readValue(actualJson, Map.class);
        assertEquals(actualMap.get("message"), exception.getMessage());
        assertEquals(actualMap.get("token"), exception.getToken());
    }
}
