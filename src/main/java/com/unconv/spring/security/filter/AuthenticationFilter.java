package com.unconv.spring.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.dto.UnconvUserDTO;
import com.unconv.spring.service.UnconvUserService;
import java.io.IOException;
import java.time.OffsetDateTime;
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

    /**
     * Constructs an {@link AuthenticationFilter} with the specified authentication manager, JWT
     * utility, and user service.
     *
     * @param customAuthenticationManager the custom authentication manager to authenticate requests
     * @param jwtUtil the JWT utility for token handling and validation
     * @param unconvUserService the user service to retrieve user details
     */
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
        } catch (NullPointerException | IOException e) {
            logger.error("attemptAuthentication", e);
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
        logger.warn("unsuccessfulAuthentication", failed);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Map<String, String> errorDetailMap = new HashMap<>();
        errorDetailMap.put("title", "Unauthorized");
        errorDetailMap.put("detail", "User Not Authenticated");
        errorDetailMap.put("timestamp", OffsetDateTime.now().toString());
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorDetailMap));
    }
}
