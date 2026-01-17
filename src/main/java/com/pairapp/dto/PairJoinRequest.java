package com.pairapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Join pair request")
public record PairJoinRequest(
        @Schema(example = "A1B2C3")
        @NotBlank String code
) {
}
