package com.college.busallocation.service;

import com.college.busallocation.exception.ApiException;
import com.college.busallocation.model.*;
import com.college.busallocation.repository.AllocationRepository;
import com.college.busallocation.repository.DeclarationRepository;
import com.college.busallocation.repository.RouteRepository;
import com.college.busallocation.repository.ShiftRepository;
import com.college.busallocation.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RouteRepository routeRepository;
    private final ShiftRepository shiftRepository;
    private final DeclarationRepository declarationRepository;
    private final AllocationRepository allocationRepository;

    public StudentService(StudentRepository studentRepository,
                          RouteRepository routeRepository,
                          ShiftRepository shiftRepository,
                          DeclarationRepository declarationRepository,
                          AllocationRepository allocationRepository) {
        this.studentRepository = studentRepository;
        this.routeRepository = routeRepository;
        this.shiftRepository = shiftRepository;
        this.declarationRepository = declarationRepository;
        this.allocationRepository = allocationRepository;
    }

    public Student getStudentProfile(String studentId, String pin) {
        if (studentId == null || studentId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Student ID cannot be empty");
        }
        Student student = studentRepository.findById(studentId.trim().toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found with ID: " + studentId));

        if (pin != null && !pin.isBlank()) {
            boolean valid = studentRepository.verifyPin(studentId, pin);
            if (!valid) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid student access PIN");
            }
        }
        return student;
    }

    /**
     * Submits or updates a student's travel declaration.
     * Enforces deadline rules, PIN verification, route & shift validation, and duplicate prevention.
     */
    public Declaration submitOrUpdateDeclaration(DeclarationRequest req) {
        // 1. Verify student identity
        if (!studentRepository.verifyPin(req.getStudentId(), req.getAccessPin())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid access PIN for student " + req.getStudentId());
        }

        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Student not found"));

        // 2. Validate route
        Route route = routeRepository.findById(req.getRouteId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Selected route does not exist"));
        if (!route.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selected route is currently inactive");
        }

        // 3. Validate shift
        Shift shift = shiftRepository.findById(req.getShiftId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Selected shift does not exist"));

        // 4. Validate travel date and deadline
        LocalDate today = LocalDate.now();
        LocalDate travelDate = req.getTravelDate();
        if (travelDate.isBefore(today)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Travel date cannot be in the past");
        }

        // Deadline check:
        // Declarations for a given date close at 21:00 (9:00 PM) on the day prior to travel date.
        // For same-day travel, cutoff is 1 hour before shift departure.
        checkDeclarationDeadline(travelDate, shift.getDepartureTime());

        // 5. Save or update declaration (ON DUPLICATE KEY UPDATE guarantees no duplicate rows)
        declarationRepository.saveOrUpdate(req);

        // 6. Return persisted record
        return declarationRepository.findByStudentAndDate(req.getStudentId(), travelDate)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve saved declaration"));
    }

    private void checkDeclarationDeadline(LocalDate travelDate, LocalTime departureTime) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (travelDate.isEqual(today)) {
            // Cutoff is 1 hour prior to departure time
            if (now.isAfter(departureTime.minusHours(1))) {
                throw new ApiException(HttpStatus.BAD_REQUEST, 
                    "Declaration deadline closed for today's " + departureTime + " shift (cutoff is 1 hour before departure)");
            }
        } else if (travelDate.isEqual(today.plusDays(1))) {
            // Tomorrow's travel: cutoff is 21:00 (9:00 PM) today
            if (now.isAfter(LocalTime.of(21, 0))) {
                throw new ApiException(HttpStatus.BAD_REQUEST, 
                    "Declaration deadline closed for tomorrow (cutoff is 9:00 PM the previous evening)");
            }
        }
    }

    public Map<String, Object> getStudentStatusAndAssignment(String studentId, LocalDate travelDate, String pin) {
        // Authenticate
        getStudentProfile(studentId, pin);

        LocalDate dateToCheck = travelDate != null ? travelDate : LocalDate.now().plusDays(1);

        Optional<Declaration> declOpt = declarationRepository.findByStudentAndDate(studentId, dateToCheck);
        Optional<StudentBusAssignment> assignOpt = allocationRepository.findStudentAssignment(studentId, dateToCheck);

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("queryDate", dateToCheck);
        result.put("declaration", declOpt.orElse(null));
        result.put("assignment", assignOpt.orElse(null));

        if (declOpt.isEmpty()) {
            result.put("statusDescription", "No travel declaration recorded for this date");
        } else if (!declOpt.get().isWillTravel()) {
            result.put("statusDescription", "Declared as Not Travelling");
        } else if (assignOpt.isPresent()) {
            StudentBusAssignment a = assignOpt.get();
            result.put("statusDescription", "Seat Allocated: " + a.getBusLabel() + " (" + a.getBusNumber() + "), Seat #" + a.getSeatNumber());
        } else {
            result.put("statusDescription", "Travel Confirmed. Bus allocation is pending administrative run.");
        }

        return result;
    }

    public List<Declaration> getStudentHistory(String studentId, String pin) {
        getStudentProfile(studentId, pin);
        return declarationRepository.findHistoryByStudent(studentId);
    }
}
