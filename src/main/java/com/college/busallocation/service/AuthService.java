package com.college.busallocation.service;

import com.college.busallocation.exception.ApiException;
import com.college.busallocation.model.AdminUser;
import com.college.busallocation.model.LoginRequest;
import com.college.busallocation.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final Map<String, TokenSession> activeSessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public static class TokenSession {
        private final int adminId;
        private final String username;
        private final String fullName;
        private final String role;
        private final Instant expiresAt;

        public TokenSession(int adminId, String username, String fullName, String role, Instant expiresAt) {
            this.adminId = adminId;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.expiresAt = expiresAt;
        }

        public int getAdminId() { return adminId; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getRole() { return role; }
        public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }

    public Map<String, Object> login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username and password are required");
        }

        AdminUser admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid admin username or password"));

        String inputHash = AdminRepository.hashPassword(request.getPassword().trim());
        if (!inputHash.equalsIgnoreCase(admin.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid admin username or password");
        }

        // Generate cryptographically secure token
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        Instant expiresAt = Instant.now().plus(4, ChronoUnit.HOURS);
        TokenSession session = new TokenSession(admin.getAdminId(), admin.getUsername(), admin.getFullName(), admin.getRole(), expiresAt);
        activeSessions.put(token, session);

        adminRepository.updateLastLogin(admin.getAdminId());

        return Map.of(
                "token", token,
                "username", admin.getUsername(),
                "fullName", admin.getFullName(),
                "role", admin.getRole(),
                "expiresAt", expiresAt.toString()
        );
    }

    public void logout(String token) {
        if (token != null) {
            activeSessions.remove(token.trim());
        }
    }

    public TokenSession validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authentication token");
        }
        TokenSession session = activeSessions.get(token.trim());
        if (session == null || session.isExpired()) {
            if (session != null) activeSessions.remove(token.trim());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Session expired or invalid. Please log in again.");
        }
        return session;
    }
}
