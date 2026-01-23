package org.clokey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.data.meilisearch")
public record MeilisearchProperties(String url, String apiKey) {}
