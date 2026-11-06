package com.college.busallocation.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class DeclarationRequest {

    @NotBlank(message = "Student ID is required")
    @Pattern(regexp = "^[A-Za-z0-9]{5,20}$", message = "Student ID must be alphanumeric and 5-20 characters")
    private String studentId;

    @NotBlank(message = "Access PIN is required to verify identity")
    private String accessPin;

    @NotNull(message = "Travel date is required")
    private LocalDate travelDate;

    @NotNull(message = "Route selection is required")
    private Integer routeId;

    @NotNull(message = "Shift selection is required")
    private Integer shiftId;

    @NotNull(message = "Must specify whether you will travel (true/false)")
    private Boolean willTravel;

    public DeclarationRequest() {}

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId != null ? studentId.trim().toUpperCase() : null;
    }

    public String getAccessPin() {
        return accessPin;
    }

    public void setAccessPin(String accessPin) {
        this.accessPin = accessPin;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public Integer getShiftId() {
        return shiftId;
    }

    public void setShiftId(Integer shiftId) {
        this.shiftId = shiftId;
    }

    public Boolean getWillTravel() {
        return willTravel;
    }

    public void setWillTravel(Boolean willTravel) {
        this.willTravel = willTravel;
    }
}
