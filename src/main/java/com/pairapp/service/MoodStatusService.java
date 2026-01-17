package com.pairapp.service;

import com.pairapp.dto.MoodResponseDto;
import com.pairapp.entity.Pair;
import com.pairapp.enums.PairStatus;
import com.pairapp.exception.ApiException;
import com.pairapp.exception.ErrorCodes;
import com.pairapp.mapper.DtoMapper;
import com.pairapp.repository.MoodResponseRepository;
import com.pairapp.repository.PairRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class MoodStatusService {
    private final PairRepository pairRepository;
    private final MoodResponseRepository moodResponseRepository;

    public MoodStatusService(PairRepository pairRepository, MoodResponseRepository moodResponseRepository) {
        this.pairRepository = pairRepository;
        this.moodResponseRepository = moodResponseRepository;
    }

    public Optional<MoodResponseDto> getPartnerStatus(UUID userId) {
        Pair pair = pairRepository.findByUserIdAndStatus(userId, PairStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR,
                        "Active pair required"));
        UUID partnerId = pair.getUserAId().equals(userId) ? pair.getUserBId() : pair.getUserAId();
        return moodResponseRepository.findLatestValidResponseForPartner(partnerId, Instant.now())
                .map(DtoMapper::toMoodResponseDto);
    }
}
