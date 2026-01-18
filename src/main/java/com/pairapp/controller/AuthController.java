package com.pairapp.controller;

import com.pairapp.dto.AuthLoginRequest;
import com.pairapp.dto.AuthRegisterRequest;
import com.pairapp.dto.AuthResponse;
import com.pairapp.dto.TelegramAuthRequest;
import com.pairapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register", responses = {
            @ApiResponse(responseCode = "200", description = "Registered", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthResponse.class),
                    examples = @ExampleObject(value = "{\"accessToken\":\"token\"}")))
    })
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Login", responses = {
            @ApiResponse(responseCode = "200", description = "Logged in", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthResponse.class),
                    examples = @ExampleObject(value = "{\"accessToken\":\"token\"}")))
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Telegram Mini App auth", responses = {
            @ApiResponse(responseCode = "200", description = "Telegram authenticated", content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AuthResponse.class),
                    examples = @ExampleObject(value = "{\\\"accessToken\\\":\\\"token\\\"}")))
    })
    @PostMapping("/telegram")
    public AuthResponse telegram(@Valid @RequestBody TelegramAuthRequest request) {
        return authService.telegramLogin(request.initData());
    }
}
