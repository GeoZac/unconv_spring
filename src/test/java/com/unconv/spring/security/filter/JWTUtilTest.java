package com.unconv.spring.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.unconv.spring.domain.UnconvUser;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JWTUtilTest {

    private JWTUtil jwtUtil;
    private String jwtSecret = "testSecret";
    private Long jwtExpiry = 3600L; // 1 hour in seconds

    @BeforeEach
    public void setUp() {
        jwtUtil = new JWTUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiry", jwtExpiry);
    }

    @Test
    public void testGenerateToken() {
        UnconvUser mockUser = mock(UnconvUser.class);
        when(mockUser.getUsername()).thenReturn("testUser");

        String token = jwtUtil.generateToken(mockUser);

        assertNotNull(token);

        DecodedJWT decodedJWT = JWT.decode(token);
        assertEquals("testUser", decodedJWT.getClaim("username").asString());
        assertEquals("unconv", decodedJWT.getIssuer());
        assertEquals("User Details", decodedJWT.getSubject());
        assertTrue(decodedJWT.getExpiresAt().after(new Date()));
    }

    @Test
    public void testValidateTokenAndRetrieveSubject() {
        UnconvUser mockUser = mock(UnconvUser.class);
        when(mockUser.getUsername()).thenReturn("testUser");

        String token = jwtUtil.generateToken(mockUser);
        String subject = jwtUtil.validateTokenAndRetrieveSubject(token);

        assertEquals("testUser", subject);
    }

    @Test
    public void testValidateTokenAndRetrieveSubjectThrowsJWTVerificationException() {
        String invalidToken = "invalidToken";

        assertThrows(
                JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveSubject(invalidToken));
    }
}
