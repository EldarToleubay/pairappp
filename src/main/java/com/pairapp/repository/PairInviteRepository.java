package com.pairapp.repository;

import com.pairapp.entity.PairInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PairInviteRepository extends JpaRepository<PairInvite, UUID> {
    Optional<PairInvite> findByCode(String code);

    boolean existsByCode(String code);

    Optional<PairInvite> findFirstByFromUserIdAndUsedAtIsNullAndExpiresAtAfter(UUID fromUserId, Instant now);
}
