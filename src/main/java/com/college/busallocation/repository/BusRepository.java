package com.college.busallocation.repository;

import com.college.busallocation.model.Bus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class BusRepository {

    private final JdbcTemplate jdbcTemplate;

    public BusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Bus> rowMapper = (rs, rowNum) -> {
        Bus bus = new Bus();
        bus.setBusId(rs.getInt("bus_id"));
        bus.setBusNumber(rs.getString("bus_number"));
        bus.setBusLabel(rs.getString("bus_label"));
        bus.setTotalCapacity(rs.getInt("total_capacity"));
        bus.setActive(rs.getBoolean("is_active"));
        if (rs.getTimestamp("created_at") != null) {
            bus.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return bus;
    };

    public List<Bus> findAll() {
        String sql = "SELECT * FROM buses ORDER BY total_capacity DESC, bus_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Bus> findAllActive() {
        String sql = "SELECT * FROM buses WHERE is_active = TRUE ORDER BY total_capacity DESC, bus_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Bus> findById(int busId) {
        String sql = "SELECT * FROM buses WHERE bus_id = ?";
        List<Bus> buses = jdbcTemplate.query(sql, rowMapper, busId);
        return buses.isEmpty() ? Optional.empty() : Optional.of(buses.get(0));
    }

    /**
     * Finds active buses that are NOT currently assigned to any route for the given travelDate and shiftId.
     * This strictly prevents the same bus from being assigned to overlapping trips.
     */
    public List<Bus> findAvailableBusesForDateAndShift(LocalDate travelDate, int shiftId) {
        String sql = """
            SELECT b.* 
            FROM buses b
            WHERE b.is_active = TRUE
              AND b.bus_id NOT IN (
                  SELECT ab.bus_id
                  FROM allocation_buses ab
                  INNER JOIN allocations a ON ab.allocation_id = a.allocation_id
                  WHERE a.travel_date = ?
                    AND a.shift_id = ?
              )
            ORDER BY b.total_capacity DESC, b.bus_id ASC
        """;
        return jdbcTemplate.query(sql, rowMapper, java.sql.Date.valueOf(travelDate), shiftId);
    }

    public int create(Bus bus) {
        String sql = "INSERT INTO buses (bus_number, bus_label, total_capacity, is_active) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, bus.getBusNumber().trim().toUpperCase());
            ps.setString(2, bus.getBusLabel().trim());
            ps.setInt(3, bus.getTotalCapacity());
            ps.setBoolean(4, bus.isActive());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
    }

    public int update(Bus bus) {
        String sql = "UPDATE buses SET bus_number = ?, bus_label = ?, total_capacity = ?, is_active = ? WHERE bus_id = ?";
        return jdbcTemplate.update(sql,
                bus.getBusNumber().trim().toUpperCase(),
                bus.getBusLabel().trim(),
                bus.getTotalCapacity(),
                bus.isActive(),
                bus.getBusId());
    }

    public int toggleActive(int busId, boolean active) {
        String sql = "UPDATE buses SET is_active = ? WHERE bus_id = ?";
        return jdbcTemplate.update(sql, active, busId);
    }
}
