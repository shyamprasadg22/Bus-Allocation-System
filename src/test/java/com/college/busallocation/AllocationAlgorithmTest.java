package com.college.busallocation;

import com.college.busallocation.model.*;
import com.college.busallocation.repository.*;
import com.college.busallocation.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AllocationAlgorithmTest {

    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private BusRepository busRepository;
    @Mock
    private DeclarationRepository declarationRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private ShiftRepository shiftRepository;

    private AllocationService allocationService;

    private final LocalDate testDate = LocalDate.now().plusDays(1);
    private final Shift testShift = new Shift(1, "SHIFT_1", "Morning", LocalTime.of(7, 30), "Morning shift");
    private final Route route1 = new Route(1, "RT-101", "Tambaram Line", "Tambaram", "Campus", "Stops...", true);
    private final Route route2 = new Route(2, "RT-102", "Central Line", "Central", "Campus", "Stops...", true);

    @BeforeEach
    void setUp() {
        allocationService = new AllocationService(
                allocationRepository,
                busRepository,
                declarationRepository,
                routeRepository,
                shiftRepository
        );
        when(shiftRepository.findById(1)).thenReturn(Optional.of(testShift));
    }

    @Test
    @DisplayName("Test 1: Empty Demand - Should record NO_DEMAND and 0 buses assigned")
    void testEmptyDemand() {
        when(routeRepository.findById(1)).thenReturn(Optional.of(route1));
        when(declarationRepository.findConfirmedStudentsForAllocation(testDate, 1, 1)).thenReturn(List.of());
        when(allocationRepository.insertAllocation(any(AllocationSummary.class))).thenReturn(101);

        AllocationRunRequest req = new AllocationRunRequest(testDate, 1, 1);
        List<AllocationSummary> results = allocationService.runAllocation(req);

        assertEquals(1, results.size());
        AllocationSummary summary = results.get(0);
        assertEquals("NO_DEMAND", summary.getStatus());
        assertEquals(0, summary.getTotalPassengers());
        assertEquals(0, summary.getTotalAllocated());
        assertEquals(0, summary.getUnallocatedCount());
        assertTrue(summary.getAssignedBuses().isEmpty());

        // Verify repeatability: previous allocation was cleaned up before run
        verify(allocationRepository).deleteExistingAllocation(testDate, 1, 1);
    }

    @Test
    @DisplayName("Test 2: Normal Capacity Fit - Should greedily allocate buses without exceeding capacity")
    void testNormalCapacityFit() {
        when(routeRepository.findById(1)).thenReturn(Optional.of(route1));

        // 35 confirmed students
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 35; i++) {
            students.add(new Student("STU" + i, "Student " + i, "s" + i + "@college.edu", "CSE", 1));
        }
        when(declarationRepository.findConfirmedStudentsForAllocation(testDate, 1, 1)).thenReturn(students);

        // Available buses: Bus A (50 capacity), Bus B (40 capacity)
        Bus busA = new Bus(1, "TN-01-A", "Bus 01", 50, true);
        Bus busB = new Bus(2, "TN-01-B", "Bus 02", 40, true);
        when(busRepository.findAvailableBusesForDateAndShift(testDate, 1)).thenReturn(List.of(busA, busB));
        when(allocationRepository.insertAllocation(any(AllocationSummary.class))).thenReturn(102);

        AllocationRunRequest req = new AllocationRunRequest(testDate, 1, 1);
        List<AllocationSummary> results = allocationService.runAllocation(req);

        assertEquals(1, results.size());
        AllocationSummary summary = results.get(0);
        assertEquals("COMPLETED", summary.getStatus());
        assertEquals(35, summary.getTotalPassengers());
        assertEquals(35, summary.getTotalAllocated());
        assertEquals(0, summary.getUnallocatedCount());
        // Greedy algorithm picked 1 bus (Bus A, cap 50) which accommodates all 35 students
        assertEquals(1, summary.getAssignedBuses().size());
        assertEquals(35, summary.getAssignedBuses().get(0).getAssignedPassengers());
        assertEquals(15, summary.getAssignedBuses().get(0).getUnusedSeats());

        // Verify student seat assignments were persisted
        verify(allocationRepository).insertStudentAssignments(argThat(assignments -> assignments.size() == 35));
    }

    @Test
    @DisplayName("Test 3: Insufficient Capacity - Should allocate available buses and flag shortfall")
    void testInsufficientCapacity() {
        when(routeRepository.findById(1)).thenReturn(Optional.of(route1));

        // 60 confirmed students
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            students.add(new Student("STU" + i, "Student " + i, "s" + i + "@college.edu", "ECE", 1));
        }
        when(declarationRepository.findConfirmedStudentsForAllocation(testDate, 1, 1)).thenReturn(students);

        // Fleet only has one 40-capacity bus available
        Bus busOnly = new Bus(3, "TN-01-C", "Bus 03", 40, true);
        when(busRepository.findAvailableBusesForDateAndShift(testDate, 1)).thenReturn(List.of(busOnly));
        when(allocationRepository.insertAllocation(any(AllocationSummary.class))).thenReturn(103);

        AllocationRunRequest req = new AllocationRunRequest(testDate, 1, 1);
        List<AllocationSummary> results = allocationService.runAllocation(req);

        assertEquals(1, results.size());
        AllocationSummary summary = results.get(0);
        assertEquals("INSUFFICIENT_CAPACITY", summary.getStatus());
        assertEquals(60, summary.getTotalPassengers());
        assertEquals(40, summary.getTotalAllocated());
        assertEquals(20, summary.getUnallocatedCount()); // 20 students unallocated!
        assertEquals(40, summary.getTotalCapacity());

        // Bus capacity must not be exceeded
        assertEquals(40, summary.getAssignedBuses().get(0).getAssignedPassengers());
        assertEquals(0, summary.getAssignedBuses().get(0).getUnusedSeats());
    }

    @Test
    @DisplayName("Test 4: Multiple Routes Allocation - Processes all active routes independently")
    void testMultipleRoutesAllocation() {
        when(routeRepository.findAllActive()).thenReturn(List.of(route1, route2));

        // Route 1: 20 students, Route 2: 0 students
        List<Student> r1Students = List.of(
                new Student("S1", "Alice", "a@c.edu", "CSE", 1),
                new Student("S2", "Bob", "b@c.edu", "CSE", 1)
        );
        when(declarationRepository.findConfirmedStudentsForAllocation(testDate, 1, 1)).thenReturn(r1Students);
        when(declarationRepository.findConfirmedStudentsForAllocation(testDate, 2, 1)).thenReturn(List.of());

        Bus busA = new Bus(1, "TN-01-A", "Bus 01", 30, true);
        when(busRepository.findAvailableBusesForDateAndShift(testDate, 1)).thenReturn(List.of(busA));

        AllocationRunRequest req = new AllocationRunRequest(testDate, 1, null); // null routeId = all active routes
        List<AllocationSummary> results = allocationService.runAllocation(req);

        assertEquals(2, results.size());
        assertEquals("COMPLETED", results.get(0).getStatus());
        assertEquals(2, results.get(0).getTotalPassengers());
        assertEquals("NO_DEMAND", results.get(1).getStatus());
        assertEquals(0, results.get(1).getTotalPassengers());
    }
}
