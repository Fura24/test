package com.example.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Reads the "app.jwt" block from application.yml (values come from environment variables). */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long accessTokenMinutes, String issuer) {
}
