package com.pairapp.service;

import com.pairapp.dto.MoodAnswerRequest;
import com.pairapp.entity.MoodRequest;
import com.pairapp.entity.Pair;
import com.pairapp.enums.BaseFeeling;
import com.pairapp.enums.MoodMode;
import com.pairapp.enums.MoodRequestStatus;
import com.pairapp.enums.PairStatus;
import com.pairapp.exception.ApiException;
import com.pairapp.repository.MoodRequestRepository;
import com.pairapp.repository.MoodResponseRepository;
import com.pairapp.repository.PairRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodRequestServiceTest {
    @Mock
    private PairRepository pairRepository;
    @Mock
    private MoodRequestRepository moodRequestRepository;
    @Mock
    private MoodResponseRepository moodResponseRepository;

    @InjectMocks
    private MoodRequestService moodRequestService;

    @Test
    void shouldRejectCooldownWithinTwoHours() {
        UUID userId = UUID.randomUUID();
        Pair pair = new Pair();
        pair.setId(UUID.randomUUID());
        pair.setUserAId(userId);
        pair.setUserBId(UUID.randomUUID());
        pair.setStatus(PairStatus.ACTIVE);

        MoodRequest last = new MoodRequest();
        last.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(pairRepository.findByUserIdAndStatus(userId, PairStatus.ACTIVE)).thenReturn(Optional.of(pair));
        when(moodRequestRepository.existsByPairIdAndStatus(pair.getId(), MoodRequestStatus.PENDING)).thenReturn(false);
        when(moodRequestRepository.countRequestsSince(pair.getId(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(1L);
        when(moodRequestRepository.findLatestByPairId(pair.getId())).thenReturn(List.of(last));

        ApiException ex = Assertions.assertThrows(ApiException.class, () -> moodRequestService.createRequest(userId));
        Assertions.assertTrue(ex.getMessage().contains("Cooldown"));
    }

    @Test
    void shouldRejectInvalidGoodMode() {
        UUID userId = UUID.randomUUID();
        MoodRequest request = new MoodRequest();
        request.setId(UUID.randomUUID());
        request.setPairId(UUID.randomUUID());
        request.setToUserId(userId);
        request.setStatus(MoodRequestStatus.PENDING);

        Pair pair = new Pair();
        pair.setId(request.getPairId());
        pair.setStatus(PairStatus.ACTIVE);

        when(moodRequestRepository.findByIdAndToUserId(request.getId(), userId)).thenReturn(Optional.of(request));
        when(pairRepository.findById(request.getPairId())).thenReturn(Optional.of(pair));
        when(moodResponseRepository.existsByRequestId(request.getId())).thenReturn(false);

        MoodAnswerRequest answer = new MoodAnswerRequest(BaseFeeling.GOOD, MoodMode.SUPPORT, List.of(), null);

        ApiException ex = Assertions.assertThrows(ApiException.class,
                () -> moodRequestService.answerRequest(userId, request.getId(), answer));
        Assertions.assertTrue(ex.getMessage().contains("Invalid mode"));
    }
}
