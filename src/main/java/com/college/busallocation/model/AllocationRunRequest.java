package com.college.busallocation.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AllocationRunRequest {

    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;

    @NotNull(message = "Shift selection is required")
    private Integer shiftId;

    // Optional: if null or 0, allocates for all active routes in that shift
    private Integer routeId;

    public AllocationRunRequest() {}

    public AllocationRunRequest(LocalDate travelDate, Integer shiftId, Integer routeId) {
        this.travelDate = travelDate;
        this.shiftId = shiftId;
        this.routeId = routeId;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public Integer getShiftId() {
        return shiftId;
    }

    public void setShiftId(Integer shiftId) {
        this.shiftId = shiftId;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }
}
