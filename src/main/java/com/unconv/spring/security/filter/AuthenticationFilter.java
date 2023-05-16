package com.unconv.spring.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.UnconvUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final CustomAuthenticationManager customAuthenticationManager;

    @Value("${jwt_secret}")
    private static String jwtSecret;

    @Value("${jwt_expiry}")
    private static Long jwtExpiry;

    public AuthenticationFilter(CustomAuthenticationManager customAuthenticationManager) {
        this.customAuthenticationManager = customAuthenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            UnconvUser user =
                    new ObjectMapper().readValue(request.getInputStream(), UnconvUser.class);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
            return customAuthenticationManager.authenticate(authentication);
        } catch (IOException e) {
            System.out.println("Got Exception in Auth Filter");
            throw new RuntimeException();
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult)
            throws IOException {
        String token =
                JWT.create()
                        .withSubject(authResult.getName())
                        .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpiry))
                        .sign(Algorithm.HMAC512(jwtSecret));
        response.getWriter().write(token);
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
