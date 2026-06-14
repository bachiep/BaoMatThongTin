package com.bachiep.sems.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Deque<Instant>> attemptsByKey = new ConcurrentHashMap<>();

    @Value("${app.security.rate-limit.login.max-requests:5}")
    private int maxLoginRequests;

    @Value("${app.security.rate-limit.login.window-seconds:60}")
    private long loginWindowSeconds;

    public boolean allowLogin(String username, String ipAddress) {
        String key = buildKey(username, ipAddress);
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(loginWindowSeconds);
        Deque<Instant> attempts = attemptsByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (attempts) {
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
                attempts.removeFirst();
            }

            if (attempts.size() >= maxLoginRequests) {
                return false;
            }

            attempts.addLast(now);
            return true;
        }
    }

    private String buildKey(String username, String ipAddress) {
        String normalizedUsername = username == null ? "unknown" : username.toLowerCase(Locale.ROOT).trim();
        String normalizedIp = ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress;
        return normalizedUsername + "|" + normalizedIp;
    }
}
