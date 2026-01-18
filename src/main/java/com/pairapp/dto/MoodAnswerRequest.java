package com.pairapp.dto;

import com.pairapp.enums.BaseFeeling;
import com.pairapp.enums.MoodAvoid;
import com.pairapp.enums.MoodMode;
import com.pairapp.enums.NotePreset;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Mood answer request")
public record MoodAnswerRequest(
        @NotNull BaseFeeling baseFeeling,
        @NotNull MoodMode mode,
        List<MoodAvoid> avoid,
        NotePreset notePreset
) {
}
