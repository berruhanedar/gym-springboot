package com.berruhanedar.app.gym_springboot.security;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private final ConcurrentMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        LoginAttempt attempt = attempts.get(normalize(username));
        if (attempt == null || attempt.blockedUntil() == null) {
            return false;
        }

        if (Instant.now().isAfter(attempt.blockedUntil())) {
            attempts.remove(normalize(username));
            return false;
        }

        return true;
    }

    public void loginFailed(String username) {
        String normalizedUsername = normalize(username);
        attempts.compute(normalizedUsername, (key, currentAttempt) -> {
            int failedAttempts = currentAttempt == null ? 1 : currentAttempt.failedAttempts() + 1;
            Instant blockedUntil = failedAttempts >= MAX_FAILED_ATTEMPTS ? Instant.now().plus(BLOCK_DURATION) : null;
            return new LoginAttempt(failedAttempts, blockedUntil);
        });
    }

    public void loginSucceeded(String username) {
        attempts.remove(normalize(username));
    }

    public long getRemainingBlockSeconds(String username) {
        LoginAttempt attempt = attempts.get(normalize(username));
        if (attempt == null || attempt.blockedUntil() == null) {
            return 0;
        }

        long remainingSeconds = Duration.between(Instant.now(), attempt.blockedUntil()).toSeconds();
        return Math.max(remainingSeconds, 0);
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private record LoginAttempt(int failedAttempts, Instant blockedUntil) {
    }
}