package com.college.busallocation.model;

import java.time.LocalDateTime;

public class Student {
    private String studentId;
    private String fullName;
    private String email;
    private String department;
    private int assignedRouteId;
    private String assignedRouteCode;
    private String assignedRouteName;
    private String phone;
    private LocalDateTime createdAt;

    public Student() {}

    public Student(String studentId, String fullName, String email, String department, int assignedRouteId) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.assignedRouteId = assignedRouteId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getAssignedRouteId() {
        return assignedRouteId;
    }

    public void setAssignedRouteId(int assignedRouteId) {
        this.assignedRouteId = assignedRouteId;
    }

    public String getAssignedRouteCode() {
        return assignedRouteCode;
    }

    public void setAssignedRouteCode(String assignedRouteCode) {
        this.assignedRouteCode = assignedRouteCode;
    }

    public String getAssignedRouteName() {
        return assignedRouteName;
    }

    public void setAssignedRouteName(String assignedRouteName) {
        this.assignedRouteName = assignedRouteName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
