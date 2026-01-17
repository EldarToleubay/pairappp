package com.pairapp.dto;

import com.pairapp.enums.BaseFeeling;
import com.pairapp.enums.MoodAvoid;
import com.pairapp.enums.MoodMode;
import com.pairapp.enums.NotePreset;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Mood response DTO")
public record MoodResponseDto(
        UUID id,
        UUID requestId,
        BaseFeeling baseFeeling,
        MoodMode mode,
        List<MoodAvoid> avoid,
        NotePreset notePreset,
        Instant validUntil,
        Instant createdAt
) {
}
