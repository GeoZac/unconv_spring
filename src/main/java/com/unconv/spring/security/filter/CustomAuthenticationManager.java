package com.unconv.spring.security.filter;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.exception.SuppressedBadCredentialsException;
import com.unconv.spring.service.UnconvUserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * CustomAuthenticationManager is a responsible for authenticating users. It implements the
 * AuthenticationManager interface.
 */
@Component
@AllArgsConstructor
public class CustomAuthenticationManager implements AuthenticationManager {

    private UnconvUserService unconvUserService;

    /**
     * Authenticates a user based on the provided authentication object.
     *
     * @param authentication the authentication object containing user credentials
     * @return an authenticated Authentication object
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        UnconvUser unconvUser =
                unconvUserService.findUnconvUserByUserName((String) authentication.getPrincipal());

        if (!bCryptPasswordEncoder()
                .matches(authentication.getCredentials().toString(), unconvUser.getPassword())) {
            throw new SuppressedBadCredentialsException("You provided an incorrect password.");
        }
        return new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(), unconvUser.getPassword());
    }

    /**
     * Creates a BCryptPasswordEncoder bean instance.
     *
     * @return a BCryptPasswordEncoder bean
     */
    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
