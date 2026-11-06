package com.college.busallocation.repository;

import com.college.busallocation.model.Route;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class RouteRepository {

    private final JdbcTemplate jdbcTemplate;

    public RouteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Route> rowMapper = (rs, rowNum) -> {
        Route route = new Route();
        route.setRouteId(rs.getInt("route_id"));
        route.setRouteCode(rs.getString("route_code"));
        route.setRouteName(rs.getString("route_name"));
        route.setStartLocation(rs.getString("start_location"));
        route.setEndLocation(rs.getString("end_location"));
        route.setStopsDescription(rs.getString("stops_description"));
        route.setActive(rs.getBoolean("is_active"));
        if (rs.getTimestamp("created_at") != null) {
            route.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return route;
    };

    public List<Route> findAllActive() {
        String sql = "SELECT * FROM routes WHERE is_active = TRUE ORDER BY route_code ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Route> findAll() {
        String sql = "SELECT * FROM routes ORDER BY route_code ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Route> findById(int routeId) {
        String sql = "SELECT * FROM routes WHERE route_id = ?";
        List<Route> routes = jdbcTemplate.query(sql, rowMapper, routeId);
        return routes.isEmpty() ? Optional.empty() : Optional.of(routes.get(0));
    }

    public int create(Route route) {
        String sql = "INSERT INTO routes (route_code, route_name, start_location, end_location, stops_description, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, route.getRouteCode().trim().toUpperCase());
            ps.setString(2, route.getRouteName().trim());
            ps.setString(3, route.getStartLocation().trim());
            ps.setString(4, route.getEndLocation() != null ? route.getEndLocation().trim() : "College Main Campus");
            ps.setString(5, route.getStopsDescription().trim());
            ps.setBoolean(6, route.isActive());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
    }

    public int update(Route route) {
        String sql = "UPDATE routes SET route_code = ?, route_name = ?, start_location = ?, end_location = ?, stops_description = ?, is_active = ? WHERE route_id = ?";
        return jdbcTemplate.update(sql,
                route.getRouteCode().trim().toUpperCase(),
                route.getRouteName().trim(),
                route.getStartLocation().trim(),
                route.getEndLocation() != null ? route.getEndLocation().trim() : "College Main Campus",
                route.getStopsDescription().trim(),
                route.isActive(),
                route.getRouteId());
    }

    public int toggleActive(int routeId, boolean active) {
        String sql = "UPDATE routes SET is_active = ? WHERE route_id = ?";
        return jdbcTemplate.update(sql, active, routeId);
    }
}
