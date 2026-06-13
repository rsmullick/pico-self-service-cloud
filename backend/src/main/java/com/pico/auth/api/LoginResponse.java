package com.pico.auth.api;

import com.pico.auth.domain.User;
import com.pico.auth.domain.UserRole;

public record LoginResponse(String id, String email, String displayName, UserRole role) {
    public static LoginResponse from(User u) {
        return new LoginResponse(u.id(), u.email(), u.displayName(), u.role());
    }
}
