package com.pairapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Auth response with access token")
public record AuthResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {
}
