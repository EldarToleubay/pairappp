package com.pairapp.dto;

import com.pairapp.enums.MoodRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Mood request response")
public record MoodRequestResponse(
        UUID id,
        UUID fromUserId,
        UUID toUserId,
        MoodRequestStatus status,
        Instant createdAt,
        Instant expiresAt
) {
}
