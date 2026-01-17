package com.pairapp.service;

import com.pairapp.dto.MoodAnswerRequest;
import com.pairapp.dto.MoodRequestResponse;
import com.pairapp.dto.MoodResponseDto;
import com.pairapp.entity.MoodRequest;
import com.pairapp.entity.MoodResponse;
import com.pairapp.entity.Pair;
import com.pairapp.enums.BaseFeeling;
import com.pairapp.enums.MoodAvoid;
import com.pairapp.enums.MoodMode;
import com.pairapp.enums.MoodRequestStatus;
import com.pairapp.enums.PairStatus;
import com.pairapp.exception.ApiException;
import com.pairapp.exception.ErrorCodes;
import com.pairapp.mapper.DtoMapper;
import com.pairapp.repository.MoodRequestRepository;
import com.pairapp.repository.MoodResponseRepository;
import com.pairapp.repository.PairRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class MoodRequestService {
    private static final EnumSet<MoodMode> GOOD_MODES = EnumSet.of(MoodMode.AS_USUAL, MoodMode.ACTIVE);
    private static final EnumSet<MoodMode> SUPPORT_MODES = EnumSet.of(MoodMode.SUPPORT, MoodMode.SILENCE,
            MoodMode.NEAR, MoodMode.NO_PRESSURE);
    private static final EnumSet<MoodAvoid> OK_AVOID = EnumSet.of(MoodAvoid.PRESSURE, MoodAvoid.MANY_MESSAGES);

    private final PairRepository pairRepository;
    private final MoodRequestRepository moodRequestRepository;
    private final MoodResponseRepository moodResponseRepository;

    public MoodRequestService(PairRepository pairRepository, MoodRequestRepository moodRequestRepository,
                              MoodResponseRepository moodResponseRepository) {
        this.pairRepository = pairRepository;
        this.moodRequestRepository = moodRequestRepository;
        this.moodResponseRepository = moodResponseRepository;
    }

    @Transactional
    public MoodRequestResponse createRequest(UUID userId) {
        Pair pair = pairRepository.findByUserIdAndStatus(userId, PairStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR,
                        "Active pair required"));
        UUID partnerId = pair.getUserAId().equals(userId) ? pair.getUserBId() : pair.getUserAId();
        if (partnerId.equals(userId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Cannot request yourself");
        }
        if (moodRequestRepository.existsByPairIdAndStatus(pair.getId(), MoodRequestStatus.PENDING)) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, "Pending request exists");
        }
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        long sentToday = moodRequestRepository.countRequestsSince(pair.getId(), startOfDay);
        if (sentToday >= 2) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.CONFLICT, "Daily limit reached");
        }
        List<MoodRequest> latest = moodRequestRepository.findLatestByPairId(pair.getId());
        if (!latest.isEmpty()) {
            MoodRequest last = latest.get(0);
            if (last.getCreatedAt().isAfter(Instant.now().minus(2, ChronoUnit.HOURS))) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.CONFLICT,
                        "Cooldown: last request was less than 2 hours ago");
            }
        }
        MoodRequest request = new MoodRequest();
        request.setPairId(pair.getId());
        request.setFromUserId(userId);
        request.setToUserId(partnerId);
        request.setStatus(MoodRequestStatus.PENDING);
        request.setExpiresAt(Instant.now().plus(60, ChronoUnit.MINUTES));
        moodRequestRepository.save(request);
        return DtoMapper.toMoodRequestResponse(request);
    }

    public List<MoodRequestResponse> getPendingRequests(UUID userId) {
        return moodRequestRepository.findByToUserIdAndStatus(userId, MoodRequestStatus.PENDING).stream()
                .map(DtoMapper::toMoodRequestResponse)
                .toList();
    }

    @Transactional
    public MoodResponseDto answerRequest(UUID userId, UUID requestId, MoodAnswerRequest answer) {
        MoodRequest request = moodRequestRepository.findByIdAndToUserId(requestId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, "Request not found"));
        if (request.getStatus() != MoodRequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, "Request already handled");
        }
        Pair pair = pairRepository.findById(request.getPairId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, "Pair not found"));
        if (pair.getStatus() != PairStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Pair not active");
        }
        if (moodResponseRepository.existsByRequestId(requestId)) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, "Response already exists");
        }
        validateAdaptive(answer);

        MoodResponse response = new MoodResponse();
        response.setRequestId(request.getId());
        response.setBaseFeeling(answer.baseFeeling());
        response.setMode(answer.mode());
        response.setAvoid(answer.avoid());
        response.setNotePreset(answer.notePreset());
        response.setValidUntil(Instant.now().plus(24, ChronoUnit.HOURS));
        moodResponseRepository.save(response);

        request.setStatus(MoodRequestStatus.ANSWERED);
        request.setAnsweredAt(Instant.now());
        moodRequestRepository.save(request);
        return DtoMapper.toMoodResponseDto(response);
    }

    private void validateAdaptive(MoodAnswerRequest answer) {
        List<MoodAvoid> avoid = answer.avoid();
        if (answer.baseFeeling() == BaseFeeling.GOOD) {
            if (!GOOD_MODES.contains(answer.mode())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Invalid mode for GOOD");
            }
            if (avoid != null && !avoid.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Avoid must be empty for GOOD");
            }
            return;
        }
        if (!SUPPORT_MODES.contains(answer.mode())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Invalid mode for feeling");
        }
        if (answer.baseFeeling() == BaseFeeling.OK) {
            if (avoid != null && !avoid.isEmpty()) {
                if (!OK_AVOID.containsAll(avoid)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR,
                            "Avoid contains invalid values for OK");
                }
            }
            return;
        }
        if (answer.baseFeeling() == BaseFeeling.HEAVY) {
            if (avoid == null || avoid.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR,
                        "Avoid must contain at least one value for HEAVY");
            }
        }
    }
}
