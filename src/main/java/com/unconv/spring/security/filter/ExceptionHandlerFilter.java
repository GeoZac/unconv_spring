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

public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final SensorAuthTokenExceptionHandler sensorAuthTokenExceptionHandler;

    public ExceptionHandlerFilter(SensorAuthTokenExceptionHandler sensorAuthTokenExceptionHandler) {
        this.sensorAuthTokenExceptionHandler = sensorAuthTokenExceptionHandler;
    }

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
