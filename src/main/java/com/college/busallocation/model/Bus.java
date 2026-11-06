package com.college.busallocation.model;

import java.time.LocalDateTime;

public class Bus {
    private int busId;
    private String busNumber;
    private String busLabel;
    private int totalCapacity;
    private boolean active;
    private LocalDateTime createdAt;

    public Bus() {}

    public Bus(int busId, String busNumber, String busLabel, int totalCapacity, boolean active) {
        this.busId = busId;
        this.busNumber = busNumber;
        this.busLabel = busLabel;
        this.totalCapacity = totalCapacity;
        this.active = active;
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

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
