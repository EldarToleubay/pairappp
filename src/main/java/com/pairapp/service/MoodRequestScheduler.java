package com.pairapp.service;

import com.pairapp.enums.MoodRequestStatus;
import com.pairapp.repository.MoodRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class MoodRequestScheduler {
    private static final Logger log = LoggerFactory.getLogger(MoodRequestScheduler.class);

    private final MoodRequestRepository moodRequestRepository;

    public MoodRequestScheduler(MoodRequestRepository moodRequestRepository) {
        this.moodRequestRepository = moodRequestRepository;
    }

    @Scheduled(fixedDelayString = "PT5M")
    @Transactional
    public void expirePendingRequests() {
        var expired = moodRequestRepository.findExpiredPending(Instant.now());
        if (expired.isEmpty()) {
            return;
        }
        expired.forEach(req -> req.setStatus(MoodRequestStatus.EXPIRED));
        moodRequestRepository.saveAll(expired);
        log.info("Expired {} mood requests", expired.size());
    }
}
