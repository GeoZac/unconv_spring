package com.unconv.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "unconv")
public class ApiVisibilityConfig {

    private boolean exposeDocs;
    private boolean exposeActuator;
}
