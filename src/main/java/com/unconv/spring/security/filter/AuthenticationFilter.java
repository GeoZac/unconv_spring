package com.unconv.spring.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.service.UnconvUserService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final CustomAuthenticationManager customAuthenticationManager;

    private final JWTUtil jwtUtil;

    private final UnconvUserService unconvUserService;

    public AuthenticationFilter(
            CustomAuthenticationManager customAuthenticationManager,
            JWTUtil jwtUtil,
            UnconvUserService unconvUserService) {
        this.customAuthenticationManager = customAuthenticationManager;
        this.jwtUtil = jwtUtil;
        this.unconvUserService = unconvUserService;
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            UnconvUserDTO unconvUserDTO =
                    new ObjectMapper().readValue(request.getInputStream(), UnconvUserDTO.class);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            unconvUserDTO.getUsername(), unconvUserDTO.getPassword());
            return customAuthenticationManager.authenticate(authentication);
        } catch (IOException e) {
            throw new AuthenticationException("Authentication failed") {};
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult)
            throws IOException {

        String username = (String) authResult.getPrincipal();

        UnconvUser unconvUser = unconvUserService.findUnconvUserByUserName(username);

        String token = jwtUtil.generateToken(unconvUser);

        // Create a response object
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("token", token);
        responseBody.put("expires", jwtUtil.getJwtExpiry());
        responseBody.put("unconvUser", unconvUser);

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
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("User Not Authenticated");
    }
}
