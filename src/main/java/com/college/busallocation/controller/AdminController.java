package com.college.busallocation.controller;

import com.college.busallocation.model.*;
import com.college.busallocation.service.AdminService;
import com.college.busallocation.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    // --- Authentication ---

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> sessionData = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", sessionData));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @GetMapping("/auth/verify")
    public ResponseEntity<ApiResponse<AuthService.TokenSession>> verifyToken(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        AuthService.TokenSession session = authService.validateToken(token);
        return ResponseEntity.ok(ApiResponse.ok("Session valid", session));
    }

    // --- Declarations grouped view ---

    @GetMapping("/declarations")
    public ResponseEntity<ApiResponse<List<Declaration>>> getDeclarations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer shiftId,
            @RequestParam(required = false) Integer routeId) {
        List<Declaration> declarations = adminService.getDeclarations(date, shiftId, routeId);
        return ResponseEntity.ok(ApiResponse.ok("Declarations retrieved", declarations));
    }

    // --- Operational Metrics ---

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.ok("Live system metrics retrieved", adminService.getLiveSystemMetrics()));
    }

    // --- Bus Fleet Management ---

    @GetMapping("/buses")
    public ResponseEntity<ApiResponse<List<Bus>>> getBuses() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllBuses()));
    }

    @PostMapping("/buses")
    public ResponseEntity<ApiResponse<Bus>> createBus(@RequestBody Bus bus) {
        Bus created = adminService.createBus(bus);
        return ResponseEntity.ok(ApiResponse.ok("Bus created successfully", created));
    }

    @PutMapping("/buses/{busId}")
    public ResponseEntity<ApiResponse<Bus>> updateBus(@PathVariable int busId, @RequestBody Bus bus) {
        Bus updated = adminService.updateBus(busId, bus);
        return ResponseEntity.ok(ApiResponse.ok("Bus updated successfully", updated));
    }

    @PatchMapping("/buses/{busId}/status")
    public ResponseEntity<ApiResponse<Void>> toggleBusStatus(@PathVariable int busId, @RequestParam boolean active) {
        adminService.toggleBusStatus(busId, active);
        return ResponseEntity.ok(ApiResponse.ok("Bus status updated", null));
    }

    // --- Routes Management ---

    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<List<Route>>> getRoutes() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllRoutes()));
    }

    @PostMapping("/routes")
    public ResponseEntity<ApiResponse<Route>> createRoute(@RequestBody Route route) {
        Route created = adminService.createRoute(route);
        return ResponseEntity.ok(ApiResponse.ok("Route created successfully", created));
    }

    @PutMapping("/routes/{routeId}")
    public ResponseEntity<ApiResponse<Route>> updateRoute(@PathVariable int routeId, @RequestBody Route route) {
        Route updated = adminService.updateRoute(routeId, route);
        return ResponseEntity.ok(ApiResponse.ok("Route updated successfully", updated));
    }

    @PatchMapping("/routes/{routeId}/status")
    public ResponseEntity<ApiResponse<Void>> toggleRouteStatus(@PathVariable int routeId, @RequestParam boolean active) {
        adminService.toggleRouteStatus(routeId, active);
        return ResponseEntity.ok(ApiResponse.ok("Route status updated", null));
    }

    @GetMapping("/shifts")
    public ResponseEntity<ApiResponse<List<Shift>>> getShifts() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllShifts()));
    }
}
