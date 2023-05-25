package com.unconv.spring.security.filter;

import com.unconv.spring.domain.UnconvUser;
import com.unconv.spring.service.UnconvUserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomAuthenticationManager implements AuthenticationManager {

    private UnconvUserService unconvUserService;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        UnconvUser unconvUser =
                unconvUserService.findUnconvUserByUserName((String) authentication.getPrincipal());

        if (!bCryptPasswordEncoder()
                .matches(authentication.getCredentials().toString(), unconvUser.getPassword())) {
            throw new BadCredentialsException("You provided an incorrect password.");
        }
        return new UsernamePasswordAuthenticationToken(
                authentication.getPrincipal(), unconvUser.getPassword());
    }

    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
