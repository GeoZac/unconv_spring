package com.unconv.spring.security;

import com.unconv.spring.security.filter.AuthenticationFilter;
import com.unconv.spring.security.filter.CustomAuthenticationManager;
import com.unconv.spring.security.filter.ExceptionHandlerFilter;
import com.unconv.spring.security.filter.JWTAuthenticationFilter;
import com.unconv.spring.security.filter.JWTUtil;
import com.unconv.spring.service.UnconvUserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@AllArgsConstructor
@Configuration
public class SecurityConfig {

    private final JWTUtil jwtUtil;

    private final CustomAuthenticationManager customAuthenticationManager;

    private final UnconvUserService unconvUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(customAuthenticationManager, jwtUtil, unconvUserService);
        authenticationFilter.setFilterProcessesUrl("/auth/login");

        http
                // Disable CSRF protection if using it in Postman
                .csrf()
                .disable()

                // Configure request authorization
                .authorizeRequests()

                // Allow specific URLs without authentication
                .antMatchers(HttpMethod.POST, "/UnconvUser")
                .permitAll()
                .antMatchers("/public/**")
                .permitAll()

                // Require authentication for any other request
                .anyRequest()
                .authenticated()

                // Configure additional filters
                .and()
                .addFilterBefore(new ExceptionHandlerFilter(), AuthenticationFilter.class)

                // Apply authentication filter only to specific URLs
                .addFilter(authenticationFilter)
                .addFilterAfter(new JWTAuthenticationFilter(jwtUtil), AuthenticationFilter.class)

                // Configure session management
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}
