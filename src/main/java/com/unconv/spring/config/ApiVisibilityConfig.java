package com.unconv.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for controlling the visibility of API-related endpoints such as Swagger
 * documentation and Spring Boot Actuator.
 *
 * <p>These properties are mapped from the application's configuration file (e.g., {@code
 * application.yml} or {@code application.properties}) using the prefix {@code unconv}.
 *
 * <pre>
 * Example usage in application.yml:
 *
 * unconv:
 *   expose-docs: true
 *   expose-actuator: false
 * </pre>
 *
 * <p>This class is a Spring {@link Component}, meaning it is automatically registered as a Spring
 * bean and available for dependency injection.
 *
 * <p>Lombok's {@link Getter} and {@link Setter} annotations are used to automatically generate the
 * getter and setter methods for the fields.
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "unconv")
public class ApiVisibilityConfig {

    private boolean exposeDocs;
    private boolean exposeActuator;
}
