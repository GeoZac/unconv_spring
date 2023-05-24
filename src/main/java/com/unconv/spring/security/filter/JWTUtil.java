package com.unconv.spring.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String jwtSecret;

    @Value("${jwt_expiry}")
    private Long jwtExpiry;

    public String generateToken(String username)
            throws IllegalArgumentException, JWTCreationException {

        Instant expirationTime = Instant.now().plus(jwtExpiry, ChronoUnit.SECONDS);

        return JWT.create()
                .withSubject("User Details")
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withIssuer("unconv")
                .withExpiresAt(expirationTime)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public String validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier =
                JWT.require(Algorithm.HMAC256(jwtSecret))
                        .withSubject("User Details")
                        .withIssuer("unconv")
                        .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("username").asString();
    }

    public Long getJwtExpiry() {
        return jwtExpiry;
    }
}
