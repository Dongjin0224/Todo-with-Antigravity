package com.todo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import java.util.List;

@Getter
@Validated
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    @NotEmpty
    private final List<String> allowedOrigins;

    @NotEmpty
    private final List<String> allowedMethods;

    @NotEmpty
    private final List<String> allowedHeaders;

    private final boolean allowCredentials;

    @Min(0)
    private final long maxAge;

    public CorsProperties(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            boolean allowCredentials,
            long maxAge
    ) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.allowCredentials = allowCredentials;
        this.maxAge = maxAge;
    }
}
