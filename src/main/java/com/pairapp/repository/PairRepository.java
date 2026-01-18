package com.pairapp.repository;

import com.pairapp.entity.Pair;
import com.pairapp.enums.PairStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PairRepository extends JpaRepository<Pair, UUID> {
    @Query("select p from Pair p where p.status = :status and (p.userAId = :userId or p.userBId = :userId)")
    Optional<Pair> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") PairStatus status);

    @Query("select p from Pair p where (p.userAId = :userA and p.userBId = :userB) or (p.userAId = :userB and p.userBId = :userA)")
    Optional<Pair> findPairByUsers(@Param("userA") UUID userA, @Param("userB") UUID userB);
}
