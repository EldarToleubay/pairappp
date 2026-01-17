package com.pairapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramUserPayload(
        long id,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String username
) {
}
