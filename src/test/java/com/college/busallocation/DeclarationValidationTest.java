package com.college.busallocation;

import com.college.busallocation.exception.ApiException;
import com.college.busallocation.model.*;
import com.college.busallocation.repository.*;
import com.college.busallocation.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeclarationValidationTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private DeclarationRepository declarationRepository;
    @Mock
    private AllocationRepository allocationRepository;

    private StudentService studentService;

    private final Student sampleStudent = new Student("2024CS101", "Aarav Sharma", "aarav@c.edu", "CSE", 1);
    private final Route sampleRoute = new Route(1, "RT-101", "Tambaram", "Tambaram", "Campus", "Stops", true);
    private final Shift sampleShift = new Shift(1, "SHIFT_1", "Morning", LocalTime.of(7, 30), "Morning shift");

    @BeforeEach
    void setUp() {
        studentService = new StudentService(
                studentRepository,
                routeRepository,
                shiftRepository,
                declarationRepository,
                allocationRepository
        );
    }

    @Test
    @DisplayName("Should reject declaration if student PIN is invalid")
    void testInvalidPinRejection() {
        when(studentRepository.verifyPin("2024CS101", "wrong_pin")).thenReturn(false);

        DeclarationRequest req = new DeclarationRequest();
        req.setStudentId("2024CS101");
        req.setAccessPin("wrong_pin");
        req.setTravelDate(LocalDate.now().plusDays(2));
        req.setRouteId(1);
        req.setShiftId(1);
        req.setWillTravel(true);

        ApiException ex = assertThrows(ApiException.class, () -> studentService.submitOrUpdateDeclaration(req));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("Invalid access PIN"));
    }

    @Test
    @DisplayName("Should reject declaration for past travel date")
    void testPastDateRejection() {
        when(studentRepository.verifyPin("2024CS101", "CS101")).thenReturn(true);
        when(studentRepository.findById("2024CS101")).thenReturn(Optional.of(sampleStudent));
        when(routeRepository.findById(1)).thenReturn(Optional.of(sampleRoute));
        when(shiftRepository.findById(1)).thenReturn(Optional.of(sampleShift));

        DeclarationRequest req = new DeclarationRequest();
        req.setStudentId("2024CS101");
        req.setAccessPin("CS101");
        req.setTravelDate(LocalDate.now().minusDays(1)); // Yesterday
        req.setRouteId(1);
        req.setShiftId(1);
        req.setWillTravel(true);

        ApiException ex = assertThrows(ApiException.class, () -> studentService.submitOrUpdateDeclaration(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("past"));
    }

    @Test
    @DisplayName("Should successfully submit and update declaration for valid future date")
    void testValidDeclarationAndRepeatableUpdate() {
        when(studentRepository.verifyPin("2024CS101", "CS101")).thenReturn(true);
        when(studentRepository.findById("2024CS101")).thenReturn(Optional.of(sampleStudent));
        when(routeRepository.findById(1)).thenReturn(Optional.of(sampleRoute));
        when(shiftRepository.findById(1)).thenReturn(Optional.of(sampleShift));

        LocalDate validDate = LocalDate.now().plusDays(3);
        DeclarationRequest req = new DeclarationRequest();
        req.setStudentId("2024CS101");
        req.setAccessPin("CS101");
        req.setTravelDate(validDate);
        req.setRouteId(1);
        req.setShiftId(1);
        req.setWillTravel(true);

        Declaration savedDecl = new Declaration();
        savedDecl.setStudentId("2024CS101");
        savedDecl.setTravelDate(validDate);
        savedDecl.setWillTravel(true);

        when(declarationRepository.findByStudentAndDate("2024CS101", validDate))
                .thenReturn(Optional.of(savedDecl));

        Declaration result = studentService.submitOrUpdateDeclaration(req);

        assertNotNull(result);
        assertEquals("2024CS101", result.getStudentId());
        assertTrue(result.isWillTravel());

        // Verify saveOrUpdate was called (which handles ON DUPLICATE KEY UPDATE)
        verify(declarationRepository).saveOrUpdate(req);
    }
}
