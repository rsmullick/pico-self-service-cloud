package com.pico.auth.api;

import com.pico.auth.application.UserStore;
import com.pico.auth.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserStore userStore;

    public AuthController(UserStore userStore) {
        this.userStore = userStore;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return userStore.authenticate(req.email(), req.password())
                .map(u -> ResponseEntity.ok(LoginResponse.from(u)))
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/users")
    public List<LoginResponse> users() {
        return userStore.allUsers().stream().map(LoginResponse::from).toList();
    }
}
