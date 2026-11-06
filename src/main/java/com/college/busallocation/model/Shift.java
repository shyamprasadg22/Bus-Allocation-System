package com.college.busallocation.model;

import java.time.LocalTime;

public class Shift {
    private int shiftId;
    private String shiftCode;
    private String shiftName;
    private LocalTime departureTime;
    private String description;

    public Shift() {}

    public Shift(int shiftId, String shiftCode, String shiftName, LocalTime departureTime, String description) {
        this.shiftId = shiftId;
        this.shiftCode = shiftCode;
        this.shiftName = shiftName;
        this.departureTime = departureTime;
        this.description = description;
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getShiftName() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName = shiftName;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
