package com.unconv.spring.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.exception.MalformedAuthTokenException;
import com.unconv.spring.exception.UnknownAuthTokenException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
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

        handler.handleUnknownAuthToken(response, exception);

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

        handler.handleMalformedAuthToken(response, exception);

        String actualJson = stringWriter.toString();

        Map actualMap = new ObjectMapper().readValue(actualJson, Map.class);
        assertEquals(actualMap.get("message"), exception.getMessage());
        assertEquals(actualMap.get("token"), exception.getToken());
    }
}
