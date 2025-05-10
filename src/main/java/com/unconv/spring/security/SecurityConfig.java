package com.unconv.spring.security;

import com.unconv.spring.security.filter.AuthenticationFilter;
import com.unconv.spring.security.filter.CustomAuthenticationManager;
import com.unconv.spring.security.filter.ExceptionHandlerFilter;
import com.unconv.spring.security.filter.JWTAuthenticationFilter;
import com.unconv.spring.security.filter.JWTUtil;
import com.unconv.spring.security.filter.SensorAuthTokenUtil;
import com.unconv.spring.service.UnconvUserService;
import com.unconv.spring.web.advice.SensorAuthTokenExceptionHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

/**
 * SecurityConfig is a configuration class responsible for configuring security settings in the
 * application. It is annotated with @Configuration to indicate that it contains bean definitions.
 */
@AllArgsConstructor
@Configuration
@EnableWebSecurity
@Import(SecurityProblemSupport.class)
public class SecurityConfig {

    private final JWTUtil jwtUtil;

    private final SensorAuthTokenUtil sensorAuthTokenUtil;

    private final CustomAuthenticationManager customAuthenticationManager;

    private final UnconvUserService unconvUserService;

    private final SensorAuthTokenExceptionHandler sensorAuthTokenExceptionHandler;

    private final SecurityProblemSupport problemSupport;

    /**
     * Configures the security filter chain for the application.
     *
     * @param http the HttpSecurity object for configuring security
     * @return the SecurityFilterChain for the application
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(customAuthenticationManager, jwtUtil, unconvUserService);
        authenticationFilter.setFilterProcessesUrl("/auth/login");

        http
                // Disable CSRF protection if using it in Postman
                .csrf(csrf -> csrf.disable())

                // Configure request authorization
                .authorizeHttpRequests(
                        requests -> {
                            requests
                                    // Allow specific URLs without authentication
                                    .requestMatchers(
                                            HttpMethod.GET, "/UnconvUser/Username/Available/**")
                                    .permitAll()
                                    .requestMatchers(HttpMethod.POST, "/UnconvUser")
                                    .permitAll()
                                    .requestMatchers("/public/**")
                                    .permitAll()
                                    .requestMatchers("/favicon.ico")
                                    .permitAll();

                            // Require authentication for any other request
                            requests.anyRequest().authenticated();
                        })
                .addFilterBefore(
                        new ExceptionHandlerFilter(sensorAuthTokenExceptionHandler),
                        AuthenticationFilter.class)

                // Apply authentication filter only to specific URLs
                .addFilter(authenticationFilter)
                .addFilterAfter(
                        new JWTAuthenticationFilter(jwtUtil, sensorAuthTokenUtil),
                        AuthenticationFilter.class)

                // Configure session management
                .sessionManagement(
                        management ->
                                management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(
                exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(problemSupport)
                                .accessDeniedHandler(problemSupport));

        return http.build();
    }
}
