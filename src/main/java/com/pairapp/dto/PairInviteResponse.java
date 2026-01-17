package com.pairapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Pair invite response")
public record PairInviteResponse(
        @Schema(example = "A1B2C3")
        String code,
        @Schema(example = "2024-06-01T12:00:00Z")
        Instant expiresAt
) {
}
