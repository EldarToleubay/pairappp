package com.pairapp.entity;

import com.pairapp.enums.BaseFeeling;
import com.pairapp.enums.MoodAvoid;
import com.pairapp.enums.MoodMode;
import com.pairapp.enums.NotePreset;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mood_responses")
@Getter
@Setter
public class MoodResponse {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "request_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "base_feeling", nullable = false)
    private BaseFeeling baseFeeling;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private MoodMode mode;

    @Convert(converter = JsonEnumListConverter.class)
    @Column(name = "avoid", columnDefinition = "jsonb")
    private List<MoodAvoid> avoid;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_preset")
    private NotePreset notePreset;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
