package com.college.busallocation.repository;

import com.college.busallocation.model.AllocationBus;
import com.college.busallocation.model.AllocationSummary;
import com.college.busallocation.model.StudentBusAssignment;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class AllocationRepository {

    private final JdbcTemplate jdbcTemplate;

    public AllocationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AllocationSummary> summaryRowMapper = (rs, rowNum) -> {
        AllocationSummary summary = new AllocationSummary();
        summary.setAllocationId(rs.getInt("allocation_id"));
        summary.setTravelDate(rs.getDate("travel_date").toLocalDate());
        summary.setShiftId(rs.getInt("shift_id"));
        summary.setShiftName(rs.getString("shift_name"));
        summary.setRouteId(rs.getInt("route_id"));
        summary.setRouteCode(rs.getString("route_code"));
        summary.setRouteName(rs.getString("route_name"));
        summary.setTotalPassengers(rs.getInt("total_passengers"));
        summary.setTotalAllocated(rs.getInt("total_allocated"));
        summary.setTotalCapacity(rs.getInt("total_capacity"));
        summary.setUnallocatedCount(rs.getInt("unallocated_count"));
        summary.setStatus(rs.getString("status"));
        if (rs.getTimestamp("allocated_at") != null) {
            summary.setAllocatedAt(rs.getTimestamp("allocated_at").toLocalDateTime());
        }
        return summary;
    };

    private final RowMapper<AllocationBus> busRowMapper = (rs, rowNum) -> {
        AllocationBus ab = new AllocationBus();
        ab.setAllocationBusId(rs.getInt("allocation_bus_id"));
        ab.setAllocationId(rs.getInt("allocation_id"));
        ab.setBusId(rs.getInt("bus_id"));
        ab.setBusNumber(rs.getString("bus_number"));
        ab.setBusLabel(rs.getString("bus_label"));
        ab.setAssignedPassengers(rs.getInt("assigned_passengers"));
        ab.setBusCapacity(rs.getInt("bus_capacity"));
        ab.setUnusedSeats(rs.getInt("unused_seats"));
        return ab;
    };

    private final RowMapper<StudentBusAssignment> assignmentRowMapper = (rs, rowNum) -> {
        StudentBusAssignment sba = new StudentBusAssignment();
        sba.setAssignmentId(rs.getInt("assignment_id"));
        sba.setAllocationId(rs.getInt("allocation_id"));
        sba.setStudentId(rs.getString("student_id"));
        sba.setStudentName(rs.getString("full_name"));
        sba.setDepartment(rs.getString("department"));
        sba.setBusId(rs.getInt("bus_id"));
        sba.setBusNumber(rs.getString("bus_number"));
        sba.setBusLabel(rs.getString("bus_label"));
        sba.setSeatNumber(rs.getInt("seat_number"));
        sba.setTravelDate(rs.getDate("travel_date").toLocalDate());
        sba.setShiftId(rs.getInt("shift_id"));
        sba.setShiftName(rs.getString("shift_name"));
        sba.setRouteId(rs.getInt("route_id"));
        sba.setRouteCode(rs.getString("route_code"));
        sba.setRouteName(rs.getString("route_name"));
        if (rs.getTimestamp("assigned_at") != null) {
            sba.setAssignedAt(rs.getTimestamp("assigned_at").toLocalDateTime());
        }
        return sba;
    };

    /**
     * Clears existing allocation for a specific route, date, and shift to ensure repeatable runs.
     * Foreign key ON DELETE CASCADE automatically removes associated allocation_buses and student_bus_assignments.
     */
    public void deleteExistingAllocation(LocalDate travelDate, int shiftId, int routeId) {
        String sql = "DELETE FROM allocations WHERE travel_date = ? AND shift_id = ? AND route_id = ?";
        jdbcTemplate.update(sql, java.sql.Date.valueOf(travelDate), shiftId, routeId);
    }

    public int insertAllocation(AllocationSummary summary) {
        String sql = """
            INSERT INTO allocations (travel_date, shift_id, route_id, total_passengers, total_allocated, total_capacity, unallocated_count, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setDate(1, java.sql.Date.valueOf(summary.getTravelDate()));
            ps.setInt(2, summary.getShiftId());
            ps.setInt(3, summary.getRouteId());
            ps.setInt(4, summary.getTotalPassengers());
            ps.setInt(5, summary.getTotalAllocated());
            ps.setInt(6, summary.getTotalCapacity());
            ps.setInt(7, summary.getUnallocatedCount());
            ps.setString(8, summary.getStatus());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
    }

    public void insertAllocationBuses(int allocationId, List<AllocationBus> buses) {
        if (buses.isEmpty()) return;
        String sql = """
            INSERT INTO allocation_buses (allocation_id, bus_id, assigned_passengers, bus_capacity, unused_seats)
            VALUES (?, ?, ?, ?, ?)
        """;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                AllocationBus bus = buses.get(i);
                ps.setInt(1, allocationId);
                ps.setInt(2, bus.getBusId());
                ps.setInt(3, bus.getAssignedPassengers());
                ps.setInt(4, bus.getBusCapacity());
                ps.setInt(5, bus.getUnusedSeats());
            }

            @Override
            public int getBatchSize() {
                return buses.size();
            }
        });
    }

    public void insertStudentAssignments(List<StudentBusAssignment> assignments) {
        if (assignments.isEmpty()) return;
        String sql = """
            INSERT INTO student_bus_assignments (allocation_id, student_id, bus_id, seat_number, travel_date, shift_id, route_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                StudentBusAssignment a = assignments.get(i);
                ps.setInt(1, a.getAllocationId());
                ps.setString(2, a.getStudentId());
                ps.setInt(3, a.getBusId());
                ps.setInt(4, a.getSeatNumber());
                ps.setDate(5, java.sql.Date.valueOf(a.getTravelDate()));
                ps.setInt(6, a.getShiftId());
                ps.setInt(7, a.getRouteId());
            }

            @Override
            public int getBatchSize() {
                return assignments.size();
            }
        });
    }

    public List<AllocationSummary> findByDateAndShift(LocalDate travelDate, Integer shiftId) {
        StringBuilder sql = new StringBuilder("""
            SELECT a.*, sh.shift_name, r.route_code, r.route_name
            FROM allocations a
            JOIN shifts sh ON a.shift_id = sh.shift_id
            JOIN routes r ON a.route_id = r.route_id
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (travelDate != null) {
            sql.append(" AND a.travel_date = ?");
            params.add(java.sql.Date.valueOf(travelDate));
        }
        if (shiftId != null && shiftId > 0) {
            sql.append(" AND a.shift_id = ?");
            params.add(shiftId);
        }

        sql.append(" ORDER BY a.travel_date DESC, a.shift_id ASC, r.route_code ASC");
        List<AllocationSummary> summaries = jdbcTemplate.query(sql.toString(), summaryRowMapper, params.toArray());

        for (AllocationSummary summary : summaries) {
            summary.setAssignedBuses(findBusesForAllocation(summary.getAllocationId()));
        }
        return summaries;
    }

    public List<AllocationBus> findBusesForAllocation(int allocationId) {
        String sql = """
            SELECT ab.*, b.bus_number, b.bus_label
            FROM allocation_buses ab
            JOIN buses b ON ab.bus_id = b.bus_id
            WHERE ab.allocation_id = ?
            ORDER BY b.bus_number ASC
        """;
        return jdbcTemplate.query(sql, busRowMapper, allocationId);
    }

    public Optional<StudentBusAssignment> findStudentAssignment(String studentId, LocalDate travelDate) {
        String sql = """
            SELECT sba.*, s.full_name, s.department, b.bus_number, b.bus_label, sh.shift_name, r.route_code, r.route_name
            FROM student_bus_assignments sba
            JOIN students s ON sba.student_id = s.student_id
            JOIN buses b ON sba.bus_id = b.bus_id
            JOIN shifts sh ON sba.shift_id = sh.shift_id
            JOIN routes r ON sba.route_id = r.route_id
            WHERE sba.student_id = ? AND sba.travel_date = ?
        """;
        List<StudentBusAssignment> list = jdbcTemplate.query(sql, assignmentRowMapper, studentId.trim().toUpperCase(), java.sql.Date.valueOf(travelDate));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<StudentBusAssignment> findAssignmentsByAllocationId(int allocationId) {
        String sql = """
            SELECT sba.*, s.full_name, s.department, b.bus_number, b.bus_label, sh.shift_name, r.route_code, r.route_name
            FROM student_bus_assignments sba
            JOIN students s ON sba.student_id = s.student_id
            JOIN buses b ON sba.bus_id = b.bus_id
            JOIN shifts sh ON sba.shift_id = sh.shift_id
            JOIN routes r ON sba.route_id = r.route_id
            WHERE sba.allocation_id = ?
            ORDER BY sba.bus_id ASC, sba.seat_number ASC
        """;
        return jdbcTemplate.query(sql, assignmentRowMapper, allocationId);
    }
}
