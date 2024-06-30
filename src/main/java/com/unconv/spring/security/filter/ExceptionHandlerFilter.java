package com.unconv.spring.security.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.unconv.spring.exception.SensorAuthTokenException;
import com.unconv.spring.web.advice.SensorAuthTokenExceptionHandler;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * ExceptionHandlerFilter is a filter that handles exceptions occurring during request processing.
 * It extends OncePerRequestFilter, ensuring that it is only executed once per request.
 */
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final SensorAuthTokenExceptionHandler sensorAuthTokenExceptionHandler;

    /**
     * Constructs an ExceptionHandlerFilter with the specified SensorAuthTokenExceptionHandler.
     *
     * @param sensorAuthTokenExceptionHandler the exception handler for sensor authentication token
     *     exceptions
     */
    public ExceptionHandlerFilter(SensorAuthTokenExceptionHandler sensorAuthTokenExceptionHandler) {
        this.sensorAuthTokenExceptionHandler = sensorAuthTokenExceptionHandler;
    }

    /**
     * Performs the actual filtering for a request.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain
     * @throws ServletException if an error occurs during servlet processing
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized");
        } catch (SensorAuthTokenException e) {
            sensorAuthTokenExceptionHandler.handleSensorAuthException(response, e);
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
