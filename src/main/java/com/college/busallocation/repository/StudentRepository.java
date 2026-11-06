package com.college.busallocation.repository;

import com.college.busallocation.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Student> rowMapper = (rs, rowNum) -> {
        Student student = new Student();
        student.setStudentId(rs.getString("student_id"));
        student.setFullName(rs.getString("full_name"));
        student.setEmail(rs.getString("email"));
        student.setDepartment(rs.getString("department"));
        student.setAssignedRouteId(rs.getInt("assigned_route_id"));
        student.setAssignedRouteCode(rs.getString("route_code"));
        student.setAssignedRouteName(rs.getString("route_name"));
        student.setPhone(rs.getString("phone"));
        if (rs.getTimestamp("created_at") != null) {
            student.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return student;
    };

    public Optional<Student> findById(String studentId) {
        String sql = """
            SELECT s.*, r.route_code, r.route_name
            FROM students s
            LEFT JOIN routes r ON s.assigned_route_id = r.route_id
            WHERE s.student_id = ?
        """;
        List<Student> students = jdbcTemplate.query(sql, rowMapper, studentId.trim().toUpperCase());
        return students.isEmpty() ? Optional.empty() : Optional.of(students.get(0));
    }

    public List<Student> findAll() {
        String sql = """
            SELECT s.*, r.route_code, r.route_name
            FROM students s
            LEFT JOIN routes r ON s.assigned_route_id = r.route_id
            ORDER BY s.student_id ASC
        """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * Verifies that the provided PIN matches the student's stored SHA-256 hash.
     * Supports exact match, case-insensitivity, leading-zero numeric padding,
     * and direct validation against the last 5 digits/characters of the student ID.
     */
    public boolean verifyPin(String studentId, String pin) {
        if (studentId == null || pin == null) {
            return false;
        }
        String cleanId = studentId.trim().toUpperCase();
        String cleanPin = pin.trim();
        if (cleanPin.isEmpty()) {
            return false;
        }

        String sql = "SELECT access_pin_hash FROM students WHERE student_id = ?";
        List<String> hashes = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("access_pin_hash"), cleanId);
        if (hashes.isEmpty() || hashes.get(0) == null) {
            return false;
        }
        String storedHash = hashes.get(0);
        String calculatedHash = sha256(cleanPin);
        String upperCalculatedHash = sha256(cleanPin.toUpperCase());
        if (storedHash.equalsIgnoreCase(calculatedHash) || storedHash.equalsIgnoreCase(upperCalculatedHash)) {
            return true;
        }

        // Support leading zeros if user typed fewer numeric digits (e.g., '1711' -> '01711')
        if (cleanPin.length() < 5 && cleanPin.matches("\\d+")) {
            String padded = String.format("%05d", Long.parseLong(cleanPin));
            if (storedHash.equalsIgnoreCase(sha256(padded))) {
                return true;
            }
        }

        // Fallback: Validate directly against the last 5 characters of the student ID
        if (cleanId.length() >= 5) {
            String last5 = cleanId.substring(cleanId.length() - 5);
            if (cleanPin.equalsIgnoreCase(last5)) {
                return true;
            }
            if (cleanPin.length() < 5 && cleanPin.matches("\\d+") && last5.matches("\\d+")) {
                if (String.format("%05d", Long.parseLong(cleanPin)).equals(last5)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
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
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
