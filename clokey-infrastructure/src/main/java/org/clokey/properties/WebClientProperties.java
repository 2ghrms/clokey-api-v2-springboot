package org.clokey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("external.api")
public record WebClientProperties(
        String aiServerIp,
        String clothInferencePath,
        String styleInferencePath,
        String clothDetectPath) {}
