package com.pairapp.service;

import com.pairapp.dto.PairInviteResponse;
import com.pairapp.dto.PairResponse;
import com.pairapp.entity.Pair;
import com.pairapp.entity.PairInvite;
import com.pairapp.entity.User;
import com.pairapp.enums.PairStatus;
import com.pairapp.exception.ApiException;
import com.pairapp.exception.ErrorCodes;
import com.pairapp.mapper.DtoMapper;
import com.pairapp.repository.PairInviteRepository;
import com.pairapp.repository.PairRepository;
import com.pairapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class PairService {
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    private final PairRepository pairRepository;
    private final PairInviteRepository pairInviteRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public PairService(PairRepository pairRepository, PairInviteRepository pairInviteRepository,
                       UserRepository userRepository) {
        this.pairRepository = pairRepository;
        this.pairInviteRepository = pairInviteRepository;
        this.userRepository = userRepository;
    }

    public PairInviteResponse createInvite(UUID userId) {
        ensureNoActivePair(userId);
        PairInvite existing = pairInviteRepository
                .findFirstByFromUserIdAndUsedAtIsNullAndExpiresAtAfter(userId, Instant.now())
                .orElse(null);
        if (existing != null) {
            return new PairInviteResponse(existing.getCode(), existing.getExpiresAt());
        }
        PairInvite invite = new PairInvite();
        invite.setFromUserId(userId);
        invite.setCode(generateCode());
        invite.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        pairInviteRepository.save(invite);
        return new PairInviteResponse(invite.getCode(), invite.getExpiresAt());
    }

    public PairResponse joinPair(UUID userId, String code) {
        PairInvite invite = pairInviteRepository.findByCode(code.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, "Invite not found"));
        if (invite.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Invite expired");
        }
        if (invite.getUsedAt() != null) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, "Invite already used");
        }
        if (invite.getFromUserId().equals(userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Cannot join your own invite");
        }
        ensureNoActivePair(userId);
        ensureNoActivePair(invite.getFromUserId());

        pairRepository.findPairByUsers(invite.getFromUserId(), userId).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, "Pair already exists");
        });

        Pair pair = new Pair();
        pair.setUserAId(invite.getFromUserId());
        pair.setUserBId(userId);
        pair.setStatus(PairStatus.ACTIVE);
        pairRepository.save(pair);
        invite.setUsedAt(Instant.now());
        pairInviteRepository.save(invite);
        User partner = userRepository.findById(invite.getFromUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, "User not found"));
        return DtoMapper.toPairResponse(pair, partner);
    }

    public PairResponse getActivePair(UUID userId) {
        Pair pair = pairRepository.findByUserIdAndStatus(userId, PairStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, "Pair not found"));
        UUID partnerId = pair.getUserAId().equals(userId) ? pair.getUserBId() : pair.getUserAId();
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, "User not found"));
        return DtoMapper.toPairResponse(pair, partner);
    }

    private void ensureNoActivePair(UUID userId) {
        pairRepository.findByUserIdAndStatus(userId, PairStatus.ACTIVE).ifPresent(pair -> {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, "User already in active pair");
        });
    }

    private String generateCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (pairInviteRepository.existsByCode(code));
        return code;
    }
}
