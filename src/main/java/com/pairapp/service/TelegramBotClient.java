package com.pairapp.service;

import com.pairapp.config.TelegramProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public String getBotUsername() {
        return properties.botUsername();
    }

    public String getMiniAppName() {
        return properties.miniAppName();
    }

    public void sendMessage(Long chatId, String text, Map<String, Object> inlineButton) {
        if (chatId == null || properties.botToken() == null || properties.botToken().isBlank()) {
            return;
        }
        if (text == null || text.isBlank()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        if (inlineButton != null) {
            payload.put("reply_markup", Map.of("inline_keyboard", List.of(List.of(inlineButton))));
        }
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
