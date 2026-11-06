package com.college.busallocation;

import com.college.busallocation.model.DeclarationRequest;
import com.college.busallocation.repository.DeclarationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DuplicatePreventionTest {

    @Autowired
    private DeclarationRepository declarationRepository;

    @Autowired
    private com.college.busallocation.repository.StudentRepository studentRepository;

    @Test
    @DisplayName("Database Test: Prevent duplicate declarations for same student and date, update cleanly")
    void testPreventDuplicateDeclarations() {
        LocalDate testDate = LocalDate.now().plusDays(10);
        String studentId = "2024CS101";

        // First submission: will_travel = true, route = 1, shift = 1
        DeclarationRequest req1 = new DeclarationRequest();
        req1.setStudentId(studentId);
        req1.setAccessPin("CS101");
        req1.setTravelDate(testDate);
        req1.setRouteId(1);
        req1.setShiftId(1);
        req1.setWillTravel(true);

        declarationRepository.saveOrUpdate(req1);

        var first = declarationRepository.findByStudentAndDate(studentId, testDate);
        assertTrue(first.isPresent());
        assertTrue(first.get().isWillTravel());
        int firstId = first.get().getDeclarationId();

        // Second submission for SAME student and date: change to will_travel = false, route = 1, shift = 2
        DeclarationRequest req2 = new DeclarationRequest();
        req2.setStudentId(studentId);
        req2.setAccessPin("CS101");
        req2.setTravelDate(testDate);
        req2.setRouteId(1);
        req2.setShiftId(2);
        req2.setWillTravel(false);

        declarationRepository.saveOrUpdate(req2);

        var second = declarationRepository.findByStudentAndDate(studentId, testDate);
        assertTrue(second.isPresent());
        assertFalse(second.get().isWillTravel());
        assertEquals(2, second.get().getShiftId());
        // Verify same declaration ID was updated, NO DUPLICATE ROW CREATED!
        assertEquals(firstId, second.get().getDeclarationId());
    }

    @Test
    @DisplayName("Database Test: Verify student PIN matches last 5 digits with case-insensitivity and zero-padding")
    void testStudentPinVerificationLast5Digits() {
        // Roll number ending in 01711
        assertTrue(studentRepository.verifyPin("250701711", "01711"));
        assertTrue(studentRepository.verifyPin("250701711", "1711")); // zero-padding

        // Roll number ending in 10711
        assertTrue(studentRepository.verifyPin("2507010711", "10711"));

        // Alphanumeric ID case-insensitivity
        assertTrue(studentRepository.verifyPin("2024CS101", "CS101"));
        assertTrue(studentRepository.verifyPin("2024CS101", "cs101"));

        // Batch mates
        assertTrue(studentRepository.verifyPin("250701712", "01712"));
        assertTrue(studentRepository.verifyPin("250701716", "01716"));

        // Rejection of invalid PIN
        assertFalse(studentRepository.verifyPin("250701711", "99999"));
        assertFalse(studentRepository.verifyPin("2024CS101", "1234"));
    }
}
