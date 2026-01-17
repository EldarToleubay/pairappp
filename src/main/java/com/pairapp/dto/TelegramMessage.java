package com.pairapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessage(
        String text,
        TelegramUserPayload from,
        @JsonProperty("chat") TelegramChat chat
) {
}
