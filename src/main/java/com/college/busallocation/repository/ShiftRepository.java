package com.college.busallocation.repository;

import com.college.busallocation.model.Shift;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ShiftRepository {

    private final JdbcTemplate jdbcTemplate;

    public ShiftRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Shift> rowMapper = (rs, rowNum) -> {
        Shift shift = new Shift();
        shift.setShiftId(rs.getInt("shift_id"));
        shift.setShiftCode(rs.getString("shift_code"));
        shift.setShiftName(rs.getString("shift_name"));
        shift.setDepartureTime(rs.getTime("departure_time").toLocalTime());
        shift.setDescription(rs.getString("description"));
        return shift;
    };

    public List<Shift> findAll() {
        String sql = "SELECT * FROM shifts ORDER BY shift_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Shift> findById(int shiftId) {
        String sql = "SELECT * FROM shifts WHERE shift_id = ?";
        List<Shift> shifts = jdbcTemplate.query(sql, rowMapper, shiftId);
        return shifts.isEmpty() ? Optional.empty() : Optional.of(shifts.get(0));
    }
}
