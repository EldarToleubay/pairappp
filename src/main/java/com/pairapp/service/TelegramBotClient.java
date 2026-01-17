package com.pairapp.service;

import com.pairapp.config.TelegramProperties;
import com.pairapp.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Service
public class TelegramBotClient {
    private static final Logger log = LoggerFactory.getLogger(TelegramBotClient.class);

    private final TelegramProperties properties;
    private final RestClient restClient;

    public TelegramBotClient(TelegramProperties properties) {
        this.properties = properties;
        String token = properties.botToken() != null ? properties.botToken() : "";
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + token)
                .build();
    }

    public void sendMoodRequestNotification(User toUser, UUID requestId) {
        if (toUser.getTelegramUserId() == null || properties.botToken() == null || properties.botToken().isBlank()) {
            return;
        }
        if (properties.botUsername() == null || properties.miniAppName() == null) {
            return;
        }
        String link = "https://t.me/" + properties.botUsername() + "/" + properties.miniAppName()
                + "?requestId=" + requestId;
        String text = "Партнёр запросил твоё состояние\n" + link;
        Map<String, Object> payload = Map.of(
                "chat_id", toUser.getTelegramUserId(),
                "text", text
        );
        try {
            restClient.post()
                    .uri("/sendMessage")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to send Telegram notification", ex);
        }
    }
}
