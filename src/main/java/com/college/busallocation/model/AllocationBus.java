package com.college.busallocation.model;

public class AllocationBus {
    private int allocationBusId;
    private int allocationId;
    private int busId;
    private String busNumber;
    private String busLabel;
    private int assignedPassengers;
    private int busCapacity;
    private int unusedSeats;

    public AllocationBus() {}

    public AllocationBus(int busId, String busNumber, String busLabel, int assignedPassengers, int busCapacity) {
        this.busId = busId;
        this.busNumber = busNumber;
        this.busLabel = busLabel;
        this.assignedPassengers = assignedPassengers;
        this.busCapacity = busCapacity;
        this.unusedSeats = Math.max(0, busCapacity - assignedPassengers);
    }

    public int getAllocationBusId() {
        return allocationBusId;
    }

    public void setAllocationBusId(int allocationBusId) {
        this.allocationBusId = allocationBusId;
    }

    public int getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(int allocationId) {
        this.allocationId = allocationId;
    }

    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusLabel() {
        return busLabel;
    }

    public void setBusLabel(String busLabel) {
        this.busLabel = busLabel;
    }

    public int getAssignedPassengers() {
        return assignedPassengers;
    }

    public void setAssignedPassengers(int assignedPassengers) {
        this.assignedPassengers = assignedPassengers;
        this.unusedSeats = Math.max(0, this.busCapacity - assignedPassengers);
    }

    public int getBusCapacity() {
        return busCapacity;
    }

    public void setBusCapacity(int busCapacity) {
        this.busCapacity = busCapacity;
        this.unusedSeats = Math.max(0, busCapacity - this.assignedPassengers);
    }

    public int getUnusedSeats() {
        return unusedSeats;
    }

    public void setUnusedSeats(int unusedSeats) {
        this.unusedSeats = unusedSeats;
    }
}
