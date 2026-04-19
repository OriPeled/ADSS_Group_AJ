/*
package dev.Workers.Tests;

import dev.Workers.domain.EmployeeTerms;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.SalaryType;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Access;
import dev.Workers.domain.Objects.Constraint;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.HR_Admin;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.Objects.WeekSchedule;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

*/
/**
 * Unified unit tests for all domain object classes.
 *
 * Tested classes:
 * - Access
 * - Constraint
 * - Employee
 * - HR_Admin
 * - Shift
 * - WeekSchedule
 *
 * The goal of this test class is to validate the basic behavior
 * of the core object model in the system.
 *//*

public class ObjectsTest {

    */
/**
     * Helper method for creating a valid EmployeeTerms object.
     *
     * @return a valid EmployeeTerms instance
     *//*

    private EmployeeTerms createTerms() {
        return new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2);
    }

    */
/**
     * Helper method for creating a valid Employee object.
     *
     * @return a valid Employee instance
     *//*

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

    // =========================================================
    // Access Tests
    // =========================================================

    */
/**
     * Verifies that the constructor stores the password correctly.
     *//*

    @Test
    void accessConstructor_shouldStorePassword() {
        Access access = new Access("1234");
        assertEquals("1234", access.getPassword());
    }

    */
/**
     * Verifies that setPassword updates the stored password.
     *//*

    @Test
    void accessSetPassword_shouldUpdatePassword() {
        Access access = new Access("1234");
        access.setPassword("9999");
        assertEquals("9999", access.getPassword());
    }

    */
/**
     * Verifies that isWrongPassword returns true for incorrect input
     * and false for the correct password.
     *//*

    @Test
    void accessIsWrongPassword_shouldReturnCorrectResult() {
        Access access = new Access("1234");

        assertTrue(access.isWrongPassword("0000"));
        assertFalse(access.isWrongPassword("1234"));
    }

    */
/**
     * Verifies password empty validation logic.
     *//*

    @Test
    void accessIsPasswordEmpty_shouldValidateInputCorrectly() {
        Access access = new Access("1234");

        assertTrue(access.isPasswordEmpty(null));
        assertTrue(access.isPasswordEmpty(""));
        assertFalse(access.isPasswordEmpty("abcd"));
    }

    // =========================================================
    // Constraint Tests
    // =========================================================

    */
/**
     * Verifies that a new Constraint initializes all week days to ShiftType.any.
     *//*

    @Test
    void constraintConstructor_shouldInitializeAllDaysToAny() {
        Constraint constraint = new Constraint();

        for (DayOfWeek day : DayOfWeek.values()) {
            assertEquals(ShiftType.any, constraint.getShiftType(day));
        }
    }

    */
/**
     * Verifies that setShiftType updates the constraint for a specific day.
     *//*

    @Test
    void constraintSetShiftType_shouldUpdateSpecificDay() {
        Constraint constraint = new Constraint();

        constraint.setShiftType(DayOfWeek.MONDAY, ShiftType.morning);

        assertEquals(ShiftType.morning, constraint.getShiftType(DayOfWeek.MONDAY));
        assertEquals(ShiftType.any, constraint.getShiftType(DayOfWeek.TUESDAY));
    }

    */
/**
     * Verifies that the internal weekly constraints map contains all seven days.
     *//*

    @Test
    void constraintGetWeekConstraints_shouldContainSevenDays() {
        Constraint constraint = new Constraint();
        assertEquals(7, constraint.getWeekConstraints().size());
    }

    // =========================================================
    // Employee Tests
    // =========================================================

    */
/**
     * Verifies that the constructor initializes all important employee fields correctly.
     *//*

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

    */
/**
     * Verifies that setName updates the employee name.
     *//*

    @Test
    void employeeSetName_shouldUpdateName() {
        Employee emp = createEmployee();

        emp.setName("Daniel");

        assertEquals("Daniel", emp.getName());
    }

    */
/**
     * Verifies that setBankAccount updates the bank account number.
     *//*

    @Test
    void employeeSetBankAccount_shouldUpdateBankAccount() {
        Employee emp = createEmployee();

        emp.setBankAccount(999999);

        assertEquals(999999, emp.getBankAccount());
    }

    */
/**
     * Verifies that setSalary updates the employee salary.
     *//*

    @Test
    void employeeSetSalary_shouldUpdateSalary() {
        Employee emp = createEmployee();

        emp.setSalary(7500.0);

        assertEquals(7500.0, emp.getSalary());
    }

    */
/**
     * Verifies that setTerms replaces the employee terms.
     *//*

    @Test
    void employeeSetTerms_shouldUpdateTerms() {
        Employee emp = createEmployee();
        EmployeeTerms newTerms = new EmployeeTerms(JobStatus.halfTime, SalaryType.hourly, 3);

        emp.setTerms(newTerms);

        assertEquals(newTerms, emp.getTerms());
    }

    */
/**
     * Verifies that terminating an employee sets the end date
     * and changes the employee status to inactive.
     *//*

    @Test
    void employeeTerminateEmployee_shouldSetEndDateAndDeactivateEmployee() {
        Employee emp = createEmployee();
        LocalDate endDate = LocalDate.of(2026, 5, 1);

        emp.terminateEmployee(endDate);

        assertEquals(endDate, emp.getEndLocalDate());
        assertFalse(emp.isActive());
    }

    */
/**
     * Verifies that toString contains basic employee details.
     *//*

    @Test
    void employeeToString_shouldContainImportantDetails() {
        Employee emp = createEmployee();
        String result = emp.toString();

        assertTrue(result.contains("Yossi"));
        assertTrue(result.contains("1"));
        assertTrue(result.contains("123456"));
        assertTrue(result.contains("5000.0"));
    }

    // =========================================================
    // HR_Admin Tests
    // =========================================================

    */
/**
     * Verifies that HR_Admin stores access details and returns the password.
     *//*

    @Test
    void hrAdminGetAccess_shouldReturnPassword() {
        HR_Admin admin = new HR_Admin(new Access("8888"));

        assertEquals("8888", admin.getAccess());
    }

    */
/**
     * Verifies that setAccess updates the admin password.
     *//*

    @Test
    void hrAdminSetAccess_shouldUpdatePassword() {
        HR_Admin admin = new HR_Admin(new Access("8888"));

        admin.setAccess("1234");

        assertEquals("1234", admin.getAccess());
    }

    // =========================================================
    // Shift Tests
    // =========================================================

    */
/**
     * Verifies that the Shift constructor stores the date and type correctly.
     *//*

    @Test
    void shiftConstructor_shouldStoreDateAndType() {
        LocalDate date = LocalDate.of(2026, 4, 20);
        Shift shift = new Shift(date, ShiftType.morning);

        assertEquals(date, shift.getShiftDate());
        assertEquals(ShiftType.morning, shift.getType());
    }

    */
/**
     * Verifies that getShiftDay returns the correct day of week.
     *//*

    @Test
    void shiftGetShiftDay_shouldReturnCorrectDayOfWeek() {
        Shift shift = new Shift(LocalDate.of(2026, 4, 19), ShiftType.morning); // Sunday

        assertEquals(DayOfWeek.SUNDAY, shift.getShiftDay());
    }

    */
/**
     * Verifies equality of two shifts with the same date and type.
     *//*

    @Test
    void shiftEquals_shouldReturnTrueForSameDateAndType() {
        Shift s1 = new Shift(LocalDate.of(2026, 4, 20), ShiftType.morning);
        Shift s2 = new Shift(LocalDate.of(2026, 4, 20), ShiftType.morning);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    */
/**
     * Verifies inequality of shifts with different dates or types.
     *//*

    @Test
    void shiftEquals_shouldReturnFalseForDifferentShiftData() {
        Shift s1 = new Shift(LocalDate.of(2026, 4, 20), ShiftType.morning);
        Shift s2 = new Shift(LocalDate.of(2026, 4, 20), ShiftType.evening);
        Shift s3 = new Shift(LocalDate.of(2026, 4, 21), ShiftType.morning);

        assertNotEquals(s1, s2);
        assertNotEquals(s1, s3);
    }

    */
/**
     * Verifies that toString contains date and shift type.
     *//*

    @Test
    void shiftToString_shouldContainDateAndType() {
        Shift shift = new Shift(LocalDate.of(2026, 4, 20), ShiftType.morning);
        String result = shift.toString();

        assertTrue(result.contains("2026-04-20"));
        assertTrue(result.contains("morning"));
    }

    // =========================================================
    // WeekSchedule Tests
    // =========================================================

    */
/**
     * Verifies that the constructor normalizes a given date
     * to the Sunday of the same week.
     *//*

    @Test
    void weekScheduleConstructor_shouldNormalizeToSunday() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22)); // Wednesday

        assertEquals(LocalDate.of(2026, 4, 19), week.getStartOfWeek());
    }

    */
/**
     * Verifies that a new week schedule is not published by default.
     *//*

    @Test
    void weekScheduleConstructor_shouldBeUnpublishedByDefault() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));

        assertFalse(week.isPublished());
    }

    */
/**
     * Verifies that setPublished updates the published flag.
     *//*

    @Test
    void weekScheduleSetPublished_shouldUpdatePublishedStatus() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));

        week.setPublished(true);

        assertTrue(week.isPublished());
    }

    */
/**
     * Verifies the calculated week status for an incomplete week.
     *//*

    @Test
    void weekScheduleCalculateStatus_shouldReturnIncompleteWhenNotFullyAssigned() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));

        assertEquals(WeekStatus.INCOMPLETE, week.calculateStatus(false));
    }

    */
/**
     * Verifies the calculated week status for a fully assigned but unpublished week.
     *//*

    @Test
    void weekScheduleCalculateStatus_shouldReturnReadyToPublishWhenFullyAssignedAndNotPublished() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));

        assertEquals(WeekStatus.READY_TO_PUBLISH, week.calculateStatus(true));
    }

    */
/**
     * Verifies the calculated week status for a fully assigned and published week.
     *//*

    @Test
    void weekScheduleCalculateStatus_shouldReturnPublishedWhenFullyAssignedAndPublished() {
        WeekSchedule week = new WeekSchedule(LocalDate.of(2026, 4, 22));
        week.setPublished(true);

        assertEquals(WeekStatus.PUBLISHED, week.calculateStatus(true));
    }

    */
/**
     * Verifies equality and hash code for week schedules
     * that belong to the same week.
     *//*

    @Test
    void weekScheduleEquals_shouldReturnTrueForSameWeek() {
        WeekSchedule w1 = new WeekSchedule(LocalDate.of(2026, 4, 20));
        WeekSchedule w2 = new WeekSchedule(LocalDate.of(2026, 4, 24));

        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }
}*/
