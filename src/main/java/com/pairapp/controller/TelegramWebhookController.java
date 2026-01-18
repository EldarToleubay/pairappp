package com.pairapp.controller;

import com.pairapp.config.TelegramProperties;
import com.pairapp.dto.TelegramUpdate;
import com.pairapp.repository.UserRepository;
import com.pairapp.service.TelegramBotClient;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telegram")
public class TelegramWebhookController {
    private final TelegramProperties properties;
    private final UserRepository userRepository;
    private final TelegramBotClient telegramBotClient;

    public TelegramWebhookController(TelegramProperties properties, UserRepository userRepository,
                                     TelegramBotClient telegramBotClient) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.telegramBotClient = telegramBotClient;
    }

    @Operation(summary = "Telegram webhook")
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestParam("secret") String secret,
                                        @RequestBody TelegramUpdate update) {
        if (properties.webhookSecret() == null || !properties.webhookSecret().equals(secret)) {
            return ResponseEntity.status(403).build();
        }
        if (update.message() == null || update.message().text() == null) {
            return ResponseEntity.ok().build();
        }
        if ("/start".equals(update.message().text())) {
            var from = update.message().from();
            var chat = update.message().chat();
            if (from == null || chat == null) {
                return ResponseEntity.ok().build();
            }
            userRepository.findByTelegramUserId(from.id())
                    .ifPresentOrElse(user -> {
                        user.setTelegramChatId(chat.id());
                        userRepository.save(user);
                        telegramBotClient.sendMessage(chat.id(), "Бот активирован. Уведомления включены ✅", null);
                    }, () -> telegramBotClient.sendMessage(chat.id(),
                            "Откройте приложение через Mini App, затем вернитесь сюда и нажмите /start", null));
        }
        return ResponseEntity.ok().build();
    }
}
