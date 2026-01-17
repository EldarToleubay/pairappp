package com.pairapp.security;

import com.pairapp.exception.ApiException;
import com.pairapp.exception.ErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, "Unauthorized");
        }
        return UUID.fromString(auth.getName());
    }
}
