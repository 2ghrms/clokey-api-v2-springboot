package org.clokey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String accessTokenSecret,
        String refreshTokenSecret,
        Long accessTokenExpirationTime,
        Long refreshTokenExpirationTime,
        String issuer) {

    public Long accessTokenExpirationMilliTime() {
        return accessTokenExpirationTime;
    }

    public Long refreshTokenExpirationMilliTime() {
        return refreshTokenExpirationTime;
    }
}
