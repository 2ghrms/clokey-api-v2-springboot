package org.clokey.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    RedisProperties.class,
    JwtProperties.class,
    S3Properties.class,
    AwsProperties.class
})
public class PropertiesConfig {}
