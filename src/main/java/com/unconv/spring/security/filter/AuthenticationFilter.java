package com.unconv.spring.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.UnconvUser;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final CustomAuthenticationManager customAuthenticationManager;

    private final JWTUtil jwtUtil;

    public AuthenticationFilter(
            CustomAuthenticationManager customAuthenticationManager, JWTUtil jwtUtil) {
        this.customAuthenticationManager = customAuthenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        UnconvUser user = new ObjectMapper().readValue(request.getInputStream(), UnconvUser.class);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        return customAuthenticationManager.authenticate(authentication);
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult)
            throws IOException {

        String token = jwtUtil.generateToken((String) authResult.getPrincipal());

        // Create a response object
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("token", token);

        // Set the response content type
        response.setContentType("application/json");

        // Write the response body as JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getWriter(), responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("User Not Authenticated");
    }
}
