package org.clokey.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    RedisProperties.class,
    JwtProperties.class,
    StorageProperties.class,
    OciProperties.class,
    WebClientProperties.class,
    FirebaseProperties.class,
    MeilisearchProperties.class
})
public class PropertiesConfig {}
