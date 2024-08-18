package com.unconv.spring.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.unconv.spring.domain.UnconvRole;
import com.unconv.spring.domain.UnconvUser;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
                .withSubject(unconvUser.getUsername())
                .withClaim(
                        "roles",
                        unconvUser.getUnconvRoles().stream()
                                .map(UnconvRole::getName)
                                .collect(Collectors.toList()))
                .withClaim("userId", unconvUser.getId().toString())
                .withIssuedAt(new Date())
                .withIssuer("unconv")
                .withExpiresAt(expirationTime)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    /**
     * Validates the given JWT token and retrieves the username (subject) contained in the token.
     *
     * @param token the JWT token to validate and decode.
     * @return the username (subject) extracted from the token.
     * @throws JWTVerificationException if the token verification fails (e.g., invalid signature,
     *     token expired).
     */
    public String validateTokenAndRetrieveUsername(String token) throws JWTVerificationException {
        JWTVerifier verifier =
                JWT.require(Algorithm.HMAC256(jwtSecret)).withIssuer("unconv").build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }

    /**
     * Validates the given JWT token and retrieves the roles contained in the token.
     *
     * @param token the JWT token to validate and decode.
     * @return a list of {@link SimpleGrantedAuthority} objects representing the roles extracted
     *     from the token.
     * @throws JWTVerificationException if the token verification fails (e.g., invalid signature,
     *     token expired).
     */
    public List<SimpleGrantedAuthority> validateTokenAndRetrieveRoles(String token) {
        JWTVerifier verifier =
                JWT.require(Algorithm.HMAC256(jwtSecret)).withIssuer("unconv").build();
        DecodedJWT jwt = verifier.verify(token);
        List<String> roleStrings = jwt.getClaim("roles").asList(String.class);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String string : roleStrings) {
            authorities.add(new SimpleGrantedAuthority(string));
        }
        return authorities;
    }
}
