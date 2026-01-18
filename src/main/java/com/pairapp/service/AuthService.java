package com.pairapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pairapp.dto.AuthLoginRequest;
import com.pairapp.dto.AuthRegisterRequest;
import com.pairapp.dto.AuthResponse;
import com.pairapp.dto.TelegramUserPayload;
import com.pairapp.entity.User;
import com.pairapp.exception.ApiException;
import com.pairapp.exception.ErrorCodes;
import com.pairapp.repository.UserRepository;
import com.pairapp.security.TelegramInitDataValidator;
import com.pairapp.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TelegramInitDataValidator telegramInitDataValidator;
    private final ObjectMapper objectMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       TelegramInitDataValidator telegramInitDataValidator, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.telegramInitDataValidator = telegramInitDataValidator;
        this.objectMapper = objectMapper;
    }

    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED,
                        "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse telegramLogin(String initData) {
        var data = telegramInitDataValidator.validate(initData);
        String userJson = data.get("user");
        if (userJson == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid Telegram auth");
        }
        TelegramUserPayload payload;
        try {
            payload = objectMapper.readValue(userJson, TelegramUserPayload.class);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Invalid Telegram auth");
        }
        User resolved = userRepository.findByTelegramUserId(payload.id())
                .orElseGet(() -> createTelegramUser(payload));
        String token = jwtService.generateToken(resolved.getId(), resolved.getEmail(), "telegram");
        return new AuthResponse(token);
    }

    private User createTelegramUser(TelegramUserPayload payload) {
        User user = new User();
        user.setTelegramUserId(payload.id());
        user.setTelegramUsername(payload.username());
        user.setFirstName(payload.firstName());
        user.setLastName(payload.lastName());
        String displayName = payload.firstName() != null ? payload.firstName() : payload.username();
        if (displayName == null && payload.lastName() != null) {
            displayName = payload.lastName();
        }
        user.setName(displayName != null ? displayName : "Telegram User");
        return userRepository.save(user);
    }
}
