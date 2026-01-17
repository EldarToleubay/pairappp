package com.pairapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.telegram")
public record TelegramProperties(
        String botToken,
        Long authMaxAgeSeconds,
        String botUsername,
        String miniAppName,
        String webhookSecret
) {
}
