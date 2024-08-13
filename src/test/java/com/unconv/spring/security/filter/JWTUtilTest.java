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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JWTUtilTest {

    private JWTUtil jwtUtil;
    private String jwtSecret = "testSecret";
    private Long jwtExpiry = 3600L; // 1 hour in seconds

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiry", jwtExpiry);
    }

    @Test
    void testGenerateToken() {
        UUID mockUserId = UUID.randomUUID();
        UnconvUser mockUser = mock(UnconvUser.class);
        when(mockUser.getUsername()).thenReturn("testUser");
        when(mockUser.getId()).thenReturn(mockUserId);

        String token = jwtUtil.generateToken(mockUser);

        assertNotNull(token);

        DecodedJWT decodedJWT = JWT.decode(token);
        assertEquals("testUser", decodedJWT.getSubject());
        assertEquals("unconv", decodedJWT.getIssuer());
        assertEquals(mockUserId.toString(), decodedJWT.getClaim("userId").asString());
        assertTrue(decodedJWT.getExpiresAt().after(new Date()));
    }

    @Test
    void testValidateTokenAndRetrieveSubject() {
        UUID mockUserId = UUID.randomUUID();
        UnconvUser mockUser = mock(UnconvUser.class);
        when(mockUser.getUsername()).thenReturn("testUser");
        when(mockUser.getId()).thenReturn(mockUserId);

        String token = jwtUtil.generateToken(mockUser);
        String subject = jwtUtil.validateTokenAndRetrieveUsername(token);

        assertEquals("testUser", subject);
    }

    @Test
    void testValidateTokenAndRetrieveSubjectThrowsJWTVerificationException() {
        String invalidToken = "invalidToken";

        assertThrows(
                JWTVerificationException.class,
                () -> jwtUtil.validateTokenAndRetrieveUsername(invalidToken));
    }
}
