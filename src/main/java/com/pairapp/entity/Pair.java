package com.pairapp.entity;

import com.pairapp.enums.PairStatus;
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
@Table(name = "pairs")
@Getter
@Setter
public class Pair {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_a_id", nullable = false, columnDefinition = "uuid")
    private UUID userAId;

    @Column(name = "user_b_id", nullable = false, columnDefinition = "uuid")
    private UUID userBId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PairStatus status;

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
