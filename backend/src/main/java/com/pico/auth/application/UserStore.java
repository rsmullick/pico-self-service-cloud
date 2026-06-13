package com.pico.auth.application;

import com.pico.auth.domain.User;
import com.pico.auth.domain.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory user store with seeded demo users.
 * A real system would use a database and hashed passwords.
 */
@Component
public class UserStore {

    private static final Map<String, User> USERS_BY_EMAIL = Map.of(
            "alice@pico.io", new User("customer-alice", "alice@pico.io", "Alice Chen", UserRole.USER),
            "bob@pico.io",   new User("customer-bob", "bob@pico.io", "Bob Tanaka", UserRole.USER),
            "admin@pico.io", new User("admin-1", "admin@pico.io", "PICO Admin", UserRole.ADMIN)
    );

    private static final Map<String, String> PASSWORDS = Map.of(
            "alice@pico.io", "demo1234",
            "bob@pico.io",   "demo1234",
            "admin@pico.io", "admin1234"
    );

    public Optional<User> authenticate(String email, String password) {
        var user = USERS_BY_EMAIL.get(email);
        if (user == null) return Optional.empty();
        var expected = PASSWORDS.get(email);
        if (!password.equals(expected)) return Optional.empty();
        return Optional.of(user);
    }

    public List<User> allUsers() {
        return List.copyOf(USERS_BY_EMAIL.values());
    }
}
