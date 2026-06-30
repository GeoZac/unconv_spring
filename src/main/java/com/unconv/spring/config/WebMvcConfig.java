package com.unconv.spring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration class that sets up CORS (Cross-Origin Resource Sharing) settings based on
 * the application properties.
 *
 * <p>This class implements {@link WebMvcConfigurer} to customize the Spring MVC configuration. It
 * is annotated with {@link Configuration} to indicate that it provides Spring-managed beans.
 *
 * <p>The required {@link ApplicationProperties} bean is injected via constructor, enabled by
 * Lombok's {@link RequiredArgsConstructor} annotation.
 *
 * <p>Example configuration in {@code application.yml} or {@code application.properties}:
 *
 * <pre>
 * application:
 *   cors:
 *     path-pattern: "/api/**"
 *     allowed-methods: "GET,POST,PUT,DELETE"
 *     allowed-headers: "*"
 *     allowed-origin-patterns: "https://example.com"
 *     allow-credentials: true
 * </pre>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /** Application-specific configuration properties that include CORS settings. */
    private final ApplicationProperties properties;

    /**
     * Configures CORS mappings for the application using values defined in {@link
     * ApplicationProperties#getCors()}.
     *
     * @param registry the {@link CorsRegistry} to which CORS configuration is applied
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(properties.getCors().getPathPattern())
                .allowedMethods(properties.getCors().getAllowedMethods())
                .allowedHeaders(properties.getCors().getAllowedHeaders())
                .allowedOriginPatterns(properties.getCors().getAllowedOriginPatterns())
                .allowCredentials(properties.getCors().isAllowCredentials());
    }
}
