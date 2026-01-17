package com.pairapp.entity;

import com.pairapp.enums.MoodRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mood_requests")
@Getter
@Setter
public class MoodRequest {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "pair_id", nullable = false, columnDefinition = "uuid")
    private UUID pairId;

    @Column(name = "from_user_id", nullable = false, columnDefinition = "uuid")
    private UUID fromUserId;

    @Column(name = "to_user_id", nullable = false, columnDefinition = "uuid")
    private UUID toUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MoodRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "answered_at")
    private Instant answeredAt;

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
