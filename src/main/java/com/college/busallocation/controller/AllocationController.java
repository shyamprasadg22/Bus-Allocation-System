package com.college.busallocation.controller;

import com.college.busallocation.model.AllocationRunRequest;
import com.college.busallocation.model.AllocationSummary;
import com.college.busallocation.model.ApiResponse;
import com.college.busallocation.model.StudentBusAssignment;
import com.college.busallocation.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/allocation")
public class AllocationController {

    private final AllocationService allocationService;

    public AllocationController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<List<AllocationSummary>>> runAllocation(
            @Valid @RequestBody AllocationRunRequest request) {
        List<AllocationSummary> summaries = allocationService.runAllocation(request);
        return ResponseEntity.ok(ApiResponse.ok("Bus allocation executed successfully", summaries));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<AllocationSummary>>> getAllocationResults(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer shiftId) {
        List<AllocationSummary> results = allocationService.getAllocationResults(date, shiftId);
        return ResponseEntity.ok(ApiResponse.ok("Allocation results retrieved", results));
    }

    @GetMapping("/{allocationId}/manifest")
    public ResponseEntity<ApiResponse<List<StudentBusAssignment>>> getPassengerManifest(
            @PathVariable int allocationId) {
        List<StudentBusAssignment> manifest = allocationService.getPassengerManifest(allocationId);
        return ResponseEntity.ok(ApiResponse.ok("Passenger manifest retrieved", manifest));
    }
}
