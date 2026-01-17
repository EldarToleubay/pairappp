package com.pairapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record AuthLoginRequest(
        @Schema(example = "alex@example.com")
        @Email @NotBlank String email,
        @Schema(example = "StrongPass123")
        @NotBlank String password
) {
}
