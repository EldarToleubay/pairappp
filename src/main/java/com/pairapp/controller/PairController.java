package com.pairapp.controller;

import com.pairapp.dto.PairInviteResponse;
import com.pairapp.dto.PairJoinRequest;
import com.pairapp.dto.PairResponse;
import com.pairapp.security.SecurityUtils;
import com.pairapp.service.PairService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pairs")
@CrossOrigin("*")
public class PairController {
    private final PairService pairService;

    public PairController(PairService pairService) {
        this.pairService = pairService;
    }

    @Operation(summary = "Create pair invite")
    @PostMapping("/invite")
    public PairInviteResponse createInvite() {
        return pairService.createInvite(SecurityUtils.currentUserId());
    }

    @Operation(summary = "Join pair by invite code")
    @PostMapping("/join")
    public PairResponse joinPair(@Valid @RequestBody PairJoinRequest request) {
        return pairService.joinPair(SecurityUtils.currentUserId(), request.code());
    }

    @Operation(summary = "Get active pair and partner")
    @GetMapping("/me")
    public PairResponse getPair() {
        return pairService.getActivePair(SecurityUtils.currentUserId());
    }
}
