package com.unconv.spring.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.unconv.spring.domain.UnconvUser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Utility class for generating and validating JSON Web Tokens (JWT). */
@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String jwtSecret;

    @Getter
    @Value("${jwt_expiry}")
    private Long jwtExpiry;

    /**
     * Generates a JWT token for the specified user.
     *
     * @param unconvUser the user for whom the token is generated
     * @return the generated JWT token
     * @throws IllegalArgumentException if the token generation fails
     * @throws JWTCreationException if token creation fails
     */
    public String generateToken(UnconvUser unconvUser)
            throws IllegalArgumentException, JWTCreationException {

        Instant expirationTime = Instant.now().plus(jwtExpiry, ChronoUnit.SECONDS);

        return JWT.create()
                .withSubject("User Details")
                .withClaim("username", unconvUser.getUsername())
                .withIssuedAt(new Date())
                .withIssuer("unconv")
                .withExpiresAt(expirationTime)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    /**
     * Validates the provided JWT token and retrieves the subject (username).
     *
     * @param token the JWT token to validate
     * @return the subject (username) extracted from the token
     * @throws JWTVerificationException if the token verification fails
     */
    public String validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier =
                JWT.require(Algorithm.HMAC256(jwtSecret))
                        .withSubject("User Details")
                        .withIssuer("unconv")
                        .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("username").asString();
    }
}
