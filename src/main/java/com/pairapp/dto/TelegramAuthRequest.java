package com.pairapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Telegram Mini App auth request")
public record TelegramAuthRequest(
        @Schema(example = "query_id=...&user=...&auth_date=...&hash=...")
        @NotBlank String initData
) {
}
