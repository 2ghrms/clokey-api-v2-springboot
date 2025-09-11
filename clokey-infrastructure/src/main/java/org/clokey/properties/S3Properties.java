package org.clokey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aws.s3")
public record S3Properties(String bucket, String endpoint) {}
