package com.pairapp.controller;

import com.pairapp.dto.MoodAnswerRequest;
import com.pairapp.dto.MoodRequestResponse;
import com.pairapp.dto.MoodResponseDto;
import com.pairapp.security.SecurityUtils;
import com.pairapp.service.MoodRequestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mood-requests")
@CrossOrigin("*")
public class MoodRequestController {
    private final MoodRequestService moodRequestService;

    public MoodRequestController(MoodRequestService moodRequestService) {
        this.moodRequestService = moodRequestService;
    }

    @Operation(summary = "Create mood request to partner")
    @PostMapping
    public MoodRequestResponse createRequest() {
        return moodRequestService.createRequest(SecurityUtils.currentUserId());
    }

    @Operation(summary = "Get pending requests for current user")
    @GetMapping("/pending")
    public List<MoodRequestResponse> getPending() {
        return moodRequestService.getPendingRequests(SecurityUtils.currentUserId());
    }

    @Operation(summary = "Answer mood request")
    @PostMapping("/{requestId}/answer")
    public MoodResponseDto answer(@PathVariable UUID requestId, @Valid @RequestBody MoodAnswerRequest request) {
        return moodRequestService.answerRequest(SecurityUtils.currentUserId(), requestId, request);
    }
}
