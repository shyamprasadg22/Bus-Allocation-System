package com.college.busallocation.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AllocationSummary {
    private int allocationId;
    private LocalDate travelDate;
    private int shiftId;
    private String shiftName;
    private int routeId;
    private String routeCode;
    private String routeName;
    private int totalPassengers;
    private int totalAllocated;
    private int totalCapacity;
    private int unallocatedCount;
    private String status; // 'COMPLETED', 'INSUFFICIENT_CAPACITY', 'NO_DEMAND'
    private LocalDateTime allocatedAt;
    private List<AllocationBus> assignedBuses = new ArrayList<>();

    public AllocationSummary() {}

    public int getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(int allocationId) {
        this.allocationId = allocationId;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getTotalPassengers() {
        return totalPassengers;
    }

    public void setTotalPassengers(int totalPassengers) {
        this.totalPassengers = totalPassengers;
    }

    public int getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(int totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public int getUnallocatedCount() {
        return unallocatedCount;
    }

    public void setUnallocatedCount(int unallocatedCount) {
        this.unallocatedCount = unallocatedCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }

    public void setAllocatedAt(LocalDateTime allocatedAt) {
        this.allocatedAt = allocatedAt;
    }

    public List<AllocationBus> getAssignedBuses() {
        return assignedBuses;
    }

    public void setAssignedBuses(List<AllocationBus> assignedBuses) {
        this.assignedBuses = assignedBuses;
    }

    public int getUnusedSeats() {
        return Math.max(0, totalCapacity - totalAllocated);
    }
}
