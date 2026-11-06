package com.college.busallocation.repository;

import com.college.busallocation.model.Declaration;
import com.college.busallocation.model.DeclarationRequest;
import com.college.busallocation.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DeclarationRepository {

    private final JdbcTemplate jdbcTemplate;

    public DeclarationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Declaration> rowMapper = (rs, rowNum) -> {
        Declaration decl = new Declaration();
        decl.setDeclarationId(rs.getInt("declaration_id"));
        decl.setStudentId(rs.getString("student_id"));
        decl.setStudentName(rs.getString("full_name"));
        decl.setDepartment(rs.getString("department"));
        decl.setTravelDate(rs.getDate("travel_date").toLocalDate());
        decl.setRouteId(rs.getInt("route_id"));
        decl.setRouteCode(rs.getString("route_code"));
        decl.setRouteName(rs.getString("route_name"));
        decl.setShiftId(rs.getInt("shift_id"));
        decl.setShiftName(rs.getString("shift_name"));
        decl.setWillTravel(rs.getBoolean("will_travel"));
        if (rs.getTimestamp("declared_at") != null) {
            decl.setDeclaredAt(rs.getTimestamp("declared_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            decl.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return decl;
    };

    public Optional<Declaration> findByStudentAndDate(String studentId, LocalDate travelDate) {
        String sql = """
            SELECT d.*, s.full_name, s.department, r.route_code, r.route_name, sh.shift_name
            FROM declarations d
            JOIN students s ON d.student_id = s.student_id
            JOIN routes r ON d.route_id = r.route_id
            JOIN shifts sh ON d.shift_id = sh.shift_id
            WHERE d.student_id = ? AND d.travel_date = ?
        """;
        List<Declaration> list = jdbcTemplate.query(sql, rowMapper, studentId.trim().toUpperCase(), java.sql.Date.valueOf(travelDate));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Atomically inserts a declaration or updates an existing declaration for the same student and date.
     * Prevents duplicate records by leveraging the unique constraint on (student_id, travel_date).
     */
    public void saveOrUpdate(DeclarationRequest req) {
        String sql = """
            INSERT INTO declarations (student_id, travel_date, route_id, shift_id, will_travel)
            VALUES (?, ?, ?, ?, ?) AS new_decl
            ON DUPLICATE KEY UPDATE
                route_id = new_decl.route_id,
                shift_id = new_decl.shift_id,
                will_travel = new_decl.will_travel,
                updated_at = CURRENT_TIMESTAMP
        """;
        jdbcTemplate.update(sql,
                req.getStudentId().trim().toUpperCase(),
                java.sql.Date.valueOf(req.getTravelDate()),
                req.getRouteId(),
                req.getShiftId(),
                req.getWillTravel());
    }

    public int countConfirmedPassengers(LocalDate travelDate, int routeId, int shiftId) {
        String sql = """
            SELECT COUNT(*) 
            FROM declarations 
            WHERE travel_date = ? 
              AND route_id = ? 
              AND shift_id = ? 
              AND will_travel = TRUE
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, java.sql.Date.valueOf(travelDate), routeId, shiftId);
        return count != null ? count : 0;
    }

    public List<Student> findConfirmedStudentsForAllocation(LocalDate travelDate, int routeId, int shiftId) {
        String sql = """
            SELECT s.*, r.route_code, r.route_name
            FROM declarations d
            JOIN students s ON d.student_id = s.student_id
            JOIN routes r ON d.route_id = r.route_id
            WHERE d.travel_date = ?
              AND d.route_id = ?
              AND d.shift_id = ?
              AND d.will_travel = TRUE
            ORDER BY d.declared_at ASC, s.student_id ASC
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Student student = new Student();
            student.setStudentId(rs.getString("student_id"));
            student.setFullName(rs.getString("full_name"));
            student.setEmail(rs.getString("email"));
            student.setDepartment(rs.getString("department"));
            student.setAssignedRouteId(rs.getInt("assigned_route_id"));
            student.setAssignedRouteCode(rs.getString("route_code"));
            student.setAssignedRouteName(rs.getString("route_name"));
            student.setPhone(rs.getString("phone"));
            return student;
        }, java.sql.Date.valueOf(travelDate), routeId, shiftId);
    }

    public List<Declaration> findDeclarations(LocalDate travelDate, Integer shiftId, Integer routeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT d.*, s.full_name, s.department, r.route_code, r.route_name, sh.shift_name
            FROM declarations d
            JOIN students s ON d.student_id = s.student_id
            JOIN routes r ON d.route_id = r.route_id
            JOIN shifts sh ON d.shift_id = sh.shift_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (travelDate != null) {
            sql.append(" AND d.travel_date = ?");
            params.add(java.sql.Date.valueOf(travelDate));
        }
        if (shiftId != null && shiftId > 0) {
            sql.append(" AND d.shift_id = ?");
            params.add(shiftId);
        }
        if (routeId != null && routeId > 0) {
            sql.append(" AND d.route_id = ?");
            params.add(routeId);
        }

        sql.append(" ORDER BY d.travel_date DESC, d.shift_id ASC, r.route_code ASC, d.declared_at ASC");
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public List<Declaration> findHistoryByStudent(String studentId) {
        String sql = """
            SELECT d.*, s.full_name, s.department, r.route_code, r.route_name, sh.shift_name
            FROM declarations d
            JOIN students s ON d.student_id = s.student_id
            JOIN routes r ON d.route_id = r.route_id
            JOIN shifts sh ON d.shift_id = sh.shift_id
            WHERE d.student_id = ?
            ORDER BY d.travel_date DESC
            LIMIT 20
        """;
        return jdbcTemplate.query(sql, rowMapper, studentId.trim().toUpperCase());
    }
}
