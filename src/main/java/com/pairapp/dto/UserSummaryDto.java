package com.pairapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Short user information")
public record UserSummaryDto(
        @Schema(example = "e8e5c323-087d-4c62-8eb1-8fb0d0bb9d0f")
        UUID id,
        @Schema(example = "Alex")
        String name
) {
}
