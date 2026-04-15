package com.unconv.spring.security.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.web.advice.SensorAuthTokenExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ExceptionHandlerFilterTest {

    @Mock private SensorAuthTokenExceptionHandler sensorAuthTokenExceptionHandler;

    @InjectMocks private ExceptionHandlerFilter exceptionHandlerFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void shouldReturn500WhenRequestInvokesRuntimeException() throws ServletException, IOException {
        doThrow(new RuntimeException("Test RuntimeException"))
                .when(filterChain)
                .doFilter(request, response);

        exceptionHandlerFilter.doFilterInternal(request, response, filterChain);

        assertEquals(MockHttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("application/json", response.getContentType());

        String jsonResponse = response.getContentAsString();
        Map<String, String> errorResponse;
        errorResponse =
                new ObjectMapper()
                        .readValue(jsonResponse, new TypeReference<Map<String, String>>() {});

        assertEquals("Internal Server Error", errorResponse.get("title"));
        assertEquals(
                "An unexpected runtime error occurred. The issue has been logged.",
                errorResponse.get("detail"));
        assertNotNull(errorResponse.get("timestamp"));

        verify(filterChain).doFilter(request, response); // Ensures the filter chain was invoked
    }
}
