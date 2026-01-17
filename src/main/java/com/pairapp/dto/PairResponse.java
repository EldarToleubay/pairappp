package com.pairapp.dto;

import com.pairapp.enums.PairStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Pair response")
public record PairResponse(
        UUID id,
        PairStatus status,
        Instant createdAt,
        UserSummaryDto partner
) {
}
