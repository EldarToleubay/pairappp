package com.pairapp.repository;

import com.pairapp.entity.MoodResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MoodResponseRepository extends JpaRepository<MoodResponse, UUID> {
    @Query("select mr from MoodResponse mr where mr.requestId in "
            + "(select req.id from MoodRequest req where req.toUserId = :toUserId) "
            + "and mr.validUntil > :now order by mr.createdAt desc")
    Optional<MoodResponse> findLatestValidResponseForPartner(@Param("toUserId") UUID toUserId,
                                                            @Param("now") Instant now);

    boolean existsByRequestId(UUID requestId);
}
