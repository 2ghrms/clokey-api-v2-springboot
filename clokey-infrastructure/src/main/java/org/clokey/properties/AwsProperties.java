package org.clokey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aws")
public record AwsProperties(String accessKeyId, String secretAccessKey, String region) {}
