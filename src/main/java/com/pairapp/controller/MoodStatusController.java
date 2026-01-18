package com.pairapp.controller;

import com.pairapp.dto.MoodResponseDto;
import com.pairapp.security.SecurityUtils;
import com.pairapp.service.MoodStatusService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mood-status")
@CrossOrigin("*")
public class MoodStatusController {
    private final MoodStatusService moodStatusService;

    public MoodStatusController(MoodStatusService moodStatusService) {
        this.moodStatusService = moodStatusService;
    }

    @Operation(summary = "Get latest valid partner mood status")
    @GetMapping("/partner")
    public ResponseEntity<MoodResponseDto> getPartnerStatus() {
        return moodStatusService.getPartnerStatus(SecurityUtils.currentUserId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
