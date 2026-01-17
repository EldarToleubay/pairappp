package com.pairapp.repository;

import com.pairapp.entity.MoodRequest;
import com.pairapp.enums.MoodRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoodRequestRepository extends JpaRepository<MoodRequest, UUID> {
    List<MoodRequest> findByToUserIdAndStatus(UUID toUserId, MoodRequestStatus status);

    boolean existsByPairIdAndStatus(UUID pairId, MoodRequestStatus status);

    @Query("select count(mr) from MoodRequest mr where mr.pairId = :pairId and mr.createdAt >= :startOfDay")
    long countRequestsSince(@Param("pairId") UUID pairId, @Param("startOfDay") Instant startOfDay);

    @Query("select mr from MoodRequest mr where mr.pairId = :pairId order by mr.createdAt desc")
    List<MoodRequest> findLatestByPairId(@Param("pairId") UUID pairId);

    @Query("select mr from MoodRequest mr where mr.status = 'PENDING' and mr.expiresAt < :now")
    List<MoodRequest> findExpiredPending(@Param("now") Instant now);

    Optional<MoodRequest> findByIdAndToUserId(UUID id, UUID toUserId);
}
