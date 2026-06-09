package org.clokey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oci")
public record OciProperties(
        String tenancyId,
        String userId,
        String fingerprint,
        String privateKey,
        String region,
        String passphrase) {}
