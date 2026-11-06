package com.college.busallocation.controller;

import com.college.busallocation.model.*;
import com.college.busallocation.repository.RouteRepository;
import com.college.busallocation.repository.ShiftRepository;
import com.college.busallocation.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final RouteRepository routeRepository;
    private final ShiftRepository shiftRepository;

    public StudentController(StudentService studentService,
                             RouteRepository routeRepository,
                             ShiftRepository shiftRepository) {
        this.studentService = studentService;
        this.routeRepository = routeRepository;
        this.shiftRepository = shiftRepository;
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Student>> getStudent(
            @PathVariable String studentId,
            @RequestParam(required = false) String pin) {
        Student student = studentService.getStudentProfile(studentId, pin);
        return ResponseEntity.ok(ApiResponse.ok("Student record retrieved", student));
    }

    @PostMapping("/declaration")
    public ResponseEntity<ApiResponse<Declaration>> submitDeclaration(
            @Valid @RequestBody DeclarationRequest request) {
        Declaration declaration = studentService.submitOrUpdateDeclaration(request);
        return ResponseEntity.ok(ApiResponse.ok("Travel declaration saved successfully", declaration));
    }

    @GetMapping("/{studentId}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            @PathVariable String studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String pin) {
        Map<String, Object> status = studentService.getStudentStatusAndAssignment(studentId, date, pin);
        return ResponseEntity.ok(ApiResponse.ok("Status retrieved", status));
    }

    @GetMapping("/{studentId}/history")
    public ResponseEntity<ApiResponse<List<Declaration>>> getHistory(
            @PathVariable String studentId,
            @RequestParam(required = false) String pin) {
        List<Declaration> history = studentService.getStudentHistory(studentId, pin);
        return ResponseEntity.ok(ApiResponse.ok("Declaration history retrieved", history));
    }

    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<List<Route>>> getActiveRoutes() {
        return ResponseEntity.ok(ApiResponse.ok(routeRepository.findAllActive()));
    }

    @GetMapping("/shifts")
    public ResponseEntity<ApiResponse<List<Shift>>> getShifts() {
        return ResponseEntity.ok(ApiResponse.ok(shiftRepository.findAll()));
    }
}
