package com.college.busallocation.model;

import java.time.LocalDateTime;

public class Route {
    private int routeId;
    private String routeCode;
    private String routeName;
    private String startLocation;
    private String endLocation;
    private String stopsDescription;
    private boolean active;
    private LocalDateTime createdAt;

    public Route() {}

    public Route(int routeId, String routeCode, String routeName, String startLocation, String endLocation, String stopsDescription, boolean active) {
        this.routeId = routeId;
        this.routeCode = routeCode;
        this.routeName = routeName;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.stopsDescription = stopsDescription;
        this.active = active;
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

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getStopsDescription() {
        return stopsDescription;
    }

    public void setStopsDescription(String stopsDescription) {
        this.stopsDescription = stopsDescription;
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
