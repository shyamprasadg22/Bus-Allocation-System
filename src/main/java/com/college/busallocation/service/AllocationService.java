package com.college.busallocation.service;

import com.college.busallocation.exception.ApiException;
import com.college.busallocation.model.*;
import com.college.busallocation.repository.AllocationRepository;
import com.college.busallocation.repository.BusRepository;
import com.college.busallocation.repository.DeclarationRepository;
import com.college.busallocation.repository.RouteRepository;
import com.college.busallocation.repository.ShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AllocationService {

    private static final Logger log = LoggerFactory.getLogger(AllocationService.class);

    private final AllocationRepository allocationRepository;
    private final BusRepository busRepository;
    private final DeclarationRepository declarationRepository;
    private final RouteRepository routeRepository;
    private final ShiftRepository shiftRepository;

    public AllocationService(AllocationRepository allocationRepository,
                             BusRepository busRepository,
                             DeclarationRepository declarationRepository,
                             RouteRepository routeRepository,
                             ShiftRepository shiftRepository) {
        this.allocationRepository = allocationRepository;
        this.busRepository = busRepository;
        this.declarationRepository = declarationRepository;
        this.routeRepository = routeRepository;
        this.shiftRepository = shiftRepository;
    }

    /**
     * Executes the greedy capacity-based allocation algorithm for a specified travel date, shift, and optional route.
     * Prevents overlapping bus assignments across trips for the same date and shift.
     * Repeatable without duplicate records.
     */
    @Transactional
    public List<AllocationSummary> runAllocation(AllocationRunRequest request) {
        LocalDate travelDate = request.getTravelDate();
        if (travelDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Travel date is required");
        }
        int shiftId = request.getShiftId();
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid shift ID: " + shiftId));

        List<Route> routesToProcess;
        if (request.getRouteId() != null && request.getRouteId() > 0) {
            Route r = routeRepository.findById(request.getRouteId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid route ID: " + request.getRouteId()));
            routesToProcess = List.of(r);
        } else {
            routesToProcess = routeRepository.findAllActive();
        }

        List<AllocationSummary> results = new ArrayList<>();

        for (Route route : routesToProcess) {
            AllocationSummary summary = allocateForSingleRoute(travelDate, shift, route);
            results.add(summary);
        }

        return results;
    }

    /**
     * Allocates buses for a single route, ensuring greedy best-capacity fit and seat assignments.
     */
    private AllocationSummary allocateForSingleRoute(LocalDate travelDate, Shift shift, Route route) {
        int routeId = route.getRouteId();
        int shiftId = shift.getShiftId();

        // 1. Clear any prior allocation for this route, date, and shift to ensure clean repeatability
        allocationRepository.deleteExistingAllocation(travelDate, shiftId, routeId);

        // 2. Count confirmed passengers who declared will_travel = true
        List<Student> confirmedStudents = declarationRepository.findConfirmedStudentsForAllocation(travelDate, routeId, shiftId);
        int passengerCount = confirmedStudents.size();

        AllocationSummary summary = new AllocationSummary();
        summary.setTravelDate(travelDate);
        summary.setShiftId(shiftId);
        summary.setShiftName(shift.getShiftName());
        summary.setRouteId(routeId);
        summary.setRouteCode(route.getRouteCode());
        summary.setRouteName(route.getRouteName());
        summary.setTotalPassengers(passengerCount);

        // Case A: Empty demand
        if (passengerCount == 0) {
            summary.setTotalAllocated(0);
            summary.setTotalCapacity(0);
            summary.setUnallocatedCount(0);
            summary.setStatus("NO_DEMAND");
            int allocId = allocationRepository.insertAllocation(summary);
            summary.setAllocationId(allocId);
            return summary;
        }

        // 3. Find available buses (active buses NOT already assigned to another route on the same date and shift)
        List<Bus> availableBuses = busRepository.findAvailableBusesForDateAndShift(travelDate, shiftId);

        // 4. Greedy capacity-based allocation
        List<AllocationBus> chosenBuses = new ArrayList<>();
        int remainingPassengers = passengerCount;
        int accumulatedCapacity = 0;

        for (Bus bus : availableBuses) {
            if (remainingPassengers <= 0) {
                break;
            }
            int capacity = bus.getTotalCapacity();
            int passengersForThisBus = Math.min(remainingPassengers, capacity);

            AllocationBus ab = new AllocationBus(bus.getBusId(), bus.getBusNumber(), bus.getBusLabel(), passengersForThisBus, capacity);
            chosenBuses.add(ab);

            remainingPassengers -= passengersForThisBus;
            accumulatedCapacity += capacity;
        }

        int totalAllocated = passengerCount - remainingPassengers;
        summary.setTotalAllocated(totalAllocated);
        summary.setTotalCapacity(accumulatedCapacity);
        summary.setUnallocatedCount(remainingPassengers);
        summary.setStatus(remainingPassengers > 0 ? "INSUFFICIENT_CAPACITY" : "COMPLETED");

        // 5. Persist allocation summary
        int allocationId = allocationRepository.insertAllocation(summary);
        summary.setAllocationId(allocationId);

        // 6. Persist allocation buses
        for (AllocationBus ab : chosenBuses) {
            ab.setAllocationId(allocationId);
        }
        allocationRepository.insertAllocationBuses(allocationId, chosenBuses);
        summary.setAssignedBuses(chosenBuses);

        // 7. Store individual student-to-bus assignments so every allocated student can identify their bus
        List<StudentBusAssignment> assignments = new ArrayList<>();
        int studentCursor = 0;

        for (AllocationBus ab : chosenBuses) {
            int seatNumber = 1;
            for (int s = 0; s < ab.getAssignedPassengers() && studentCursor < confirmedStudents.size(); s++) {
                Student st = confirmedStudents.get(studentCursor++);
                StudentBusAssignment assignment = new StudentBusAssignment();
                assignment.setAllocationId(allocationId);
                assignment.setStudentId(st.getStudentId());
                assignment.setBusId(ab.getBusId());
                assignment.setSeatNumber(seatNumber++);
                assignment.setTravelDate(travelDate);
                assignment.setShiftId(shiftId);
                assignment.setRouteId(routeId);
                assignments.add(assignment);
            }
        }

        allocationRepository.insertStudentAssignments(assignments);

        log.info("Allocation completed for Route {} ({}) on {} shift {}: {}/{} allocated. Status: {}",
                route.getRouteCode(), route.getRouteName(), travelDate, shift.getShiftCode(), totalAllocated, passengerCount, summary.getStatus());

        return summary;
    }

    public List<AllocationSummary> getAllocationResults(LocalDate travelDate, Integer shiftId) {
        return allocationRepository.findByDateAndShift(travelDate, shiftId);
    }

    public List<StudentBusAssignment> getPassengerManifest(int allocationId) {
        return allocationRepository.findAssignmentsByAllocationId(allocationId);
    }
}
