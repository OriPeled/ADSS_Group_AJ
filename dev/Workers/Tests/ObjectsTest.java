package dev.Workers.Tests;

import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Access;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.WeekSchedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unified unit tests for core domain objects.
 */
public class ObjectsTest {

    private EmployeeTerms createTerms() {
        return new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2);
    }

    private Employee createEmployee() {
        return new Employee(
                "Yossi",
                1,
                123456,
                5000.0,
                createTerms(),
                LocalDate.of(2026, 4, 19)
        );
    }

    /**
     * Verifies password validation logic.
     */
    @Test
    void accessIsWrongPassword_shouldReturnCorrectResult() {
        Access access = new Access("1234");

        assertTrue(access.isWrongPassword("0000"));
        assertFalse(access.isWrongPassword("1234"));
    }

    /**
     * Verifies that employee constructor initializes fields correctly.
     */
    @Test
    void employeeConstructor_shouldInitializeFieldsCorrectly() {
        Employee emp = createEmployee();

        assertEquals("Yossi", emp.getName());
        assertEquals(1, emp.getId());
        assertEquals(123456, emp.getBankAccount());
        assertEquals(5000.0, emp.getSalary());
        assertEquals(LocalDate.of(2026, 4, 19), emp.getStartLocalDate());
        assertNull(emp.getEndLocalDate());
        assertTrue(emp.isActive());
    }

    /**
     * Verifies that terminating an employee deactivates them.
     */
    @Test
    void employeeTerminateEmployee_shouldDeactivateEmployee() {
        Employee emp = createEmployee();
        LocalDate endDate = LocalDate.of(2026, 5, 1);

        emp.terminateEmployee(endDate);

        assertEquals(endDate, emp.getEndLocalDate());
        assertFalse(emp.isActive());
    }

    /**
     * Verifies that WeekSchedule normalizes date to Sunday.
     */
    @Test
    void weekScheduleConstructor_shouldNormalizeToSunday() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));

        assertEquals(LocalDate.of(2026, 4, 19), week.getStartOfWeek());
    }

    /**
     * Verifies that a new week is not published by default.
     */
    @Test
    void weekSchedule_shouldBeUnpublishedByDefault() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));

        assertFalse(week.isPublished());
    }

    /**
     * Verifies week status logic.
     */
    @Test
    void weekScheduleCalculateStatus_shouldWorkCorrectly() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));

        assertEquals(WeekStatus.INCOMPLETE, week.calculateStatus(false));
        assertEquals(WeekStatus.READY_TO_PUBLISH, week.calculateStatus(true));

        week.setPublished(true);
        assertEquals(WeekStatus.PUBLISHED, week.calculateStatus(true));
    }

    /**
     * Verifies equality of week schedules.
     */
    @Test
    void weekScheduleEquals_shouldReturnTrueForSameWeek() {
        WeekSchedule w1 = new WeekSchedule(LocalDate.of(2026, 4, 20));
        WeekSchedule w2 = new WeekSchedule(LocalDate.of(2026, 4, 24));

        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }
}