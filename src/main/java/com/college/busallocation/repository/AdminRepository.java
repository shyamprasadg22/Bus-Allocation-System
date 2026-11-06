package com.college.busallocation.repository;

import com.college.busallocation.model.AdminUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AdminUser> rowMapper = (rs, rowNum) -> {
        AdminUser admin = new AdminUser();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setUsername(rs.getString("username"));
        admin.setPasswordHash(rs.getString("password_hash"));
        admin.setFullName(rs.getString("full_name"));
        admin.setRole(rs.getString("role"));
        admin.setActive(rs.getBoolean("is_active"));
        if (rs.getTimestamp("last_login") != null) {
            admin.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
        }
        return admin;
    };

    public Optional<AdminUser> findByUsername(String username) {
        String sql = "SELECT * FROM admin_users WHERE username = ? AND is_active = TRUE";
        List<AdminUser> list = jdbcTemplate.query(sql, rowMapper, username.trim().toLowerCase());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void updateLastLogin(int adminId) {
        String sql = "UPDATE admin_users SET last_login = CURRENT_TIMESTAMP WHERE admin_id = ?";
        jdbcTemplate.update(sql, adminId);
    }

    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
