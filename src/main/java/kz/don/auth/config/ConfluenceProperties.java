package kz.don.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "confluence")
public class ConfluenceProperties {

    private String baseUrl;
    private String apiToken;
    private String username;
    private String defaultSpaceKey;
    private int connectionTimeout = 30000;
    private int requestTimeout = 30000;
}