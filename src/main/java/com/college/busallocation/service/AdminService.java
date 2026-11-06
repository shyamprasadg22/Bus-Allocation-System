package com.college.busallocation.service;

import com.college.busallocation.exception.ApiException;
import com.college.busallocation.model.Bus;
import com.college.busallocation.model.Declaration;
import com.college.busallocation.model.Route;
import com.college.busallocation.model.Shift;
import com.college.busallocation.repository.BusRepository;
import com.college.busallocation.repository.DeclarationRepository;
import com.college.busallocation.repository.RouteRepository;
import com.college.busallocation.repository.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final ShiftRepository shiftRepository;
    private final DeclarationRepository declarationRepository;

    public AdminService(BusRepository busRepository,
                        RouteRepository routeRepository,
                        ShiftRepository shiftRepository,
                        DeclarationRepository declarationRepository) {
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.shiftRepository = shiftRepository;
        this.declarationRepository = declarationRepository;
    }

    public List<Declaration> getDeclarations(LocalDate travelDate, Integer shiftId, Integer routeId) {
        return declarationRepository.findDeclarations(travelDate, shiftId, routeId);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public Bus createBus(Bus bus) {
        if (bus.getBusNumber() == null || bus.getBusNumber().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Bus registration number is required");
        }
        if (bus.getTotalCapacity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Bus seating capacity must be greater than 0");
        }
        int id = busRepository.create(bus);
        bus.setBusId(id);
        return bus;
    }

    public Bus updateBus(int busId, Bus bus) {
        busRepository.findById(busId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found with ID: " + busId));

        bus.setBusId(busId);
        if (bus.getTotalCapacity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Bus seating capacity must be greater than 0");
        }
        busRepository.update(bus);
        return busRepository.findById(busId).orElse(bus);
    }

    public void toggleBusStatus(int busId, boolean active) {
        busRepository.findById(busId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bus not found with ID: " + busId));
        busRepository.toggleActive(busId, active);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route createRoute(Route route) {
        if (route.getRouteCode() == null || route.getRouteCode().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Route code is required (e.g., RT-106)");
        }
        if (route.getRouteName() == null || route.getRouteName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Route name is required");
        }
        int id = routeRepository.create(route);
        route.setRouteId(id);
        return route;
    }

    public Route updateRoute(int routeId, Route route) {
        routeRepository.findById(routeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Route not found with ID: " + routeId));
        route.setRouteId(routeId);
        routeRepository.update(route);
        return routeRepository.findById(routeId).orElse(route);
    }

    public void toggleRouteStatus(int routeId, boolean active) {
        routeRepository.findById(routeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Route not found with ID: " + routeId));
        routeRepository.toggleActive(routeId, active);
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    /**
     * Returns factual, un-fabricated live database counts.
     */
    public Map<String, Object> getLiveSystemMetrics() {
        List<Bus> buses = busRepository.findAll();
        long activeBuses = buses.stream().filter(Bus::isActive).count();
        int totalCapacity = buses.stream().filter(Bus::isActive).mapToInt(Bus::getTotalCapacity).sum();

        List<Route> routes = routeRepository.findAll();
        long activeRoutes = routes.stream().filter(Route::isActive).count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalBuses", buses.size());
        metrics.put("activeBuses", activeBuses);
        metrics.put("totalActiveFleetCapacity", totalCapacity);
        metrics.put("totalRoutes", routes.size());
        metrics.put("activeRoutes", activeRoutes);
        return metrics;
    }
}
