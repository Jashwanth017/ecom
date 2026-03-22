package com.sample.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record AdminSeedProperties(
        boolean enabled,
        String email,
        String password
) {
}
