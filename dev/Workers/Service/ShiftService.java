package dev.Workers.Service;

import dev.Workers.domain.Assignments;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.RoleManager;
import dev.Workers.domain.ShiftManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * ShiftService acts as the entry point for UI/Web controllers.
 * It delegates all state management and business rules to the ShiftManager.
 */
public class ShiftService {
    private final ShiftManager shiftManager;
    private static ShiftService instance;
    private static final EmployeeManager employeeManager = EmployeeManager.getInstance();
    private static final RoleManager roleManager = RoleManager.getInstance();

    private static Assignments assignments;

    /**
     * Singleton access
     */
    public static ShiftService getInstance() {
        if (instance == null) {
            instance = new ShiftService();
        }
        return instance;
    }

    private ShiftService() {
        this.shiftManager = ShiftManager.getInstance();
    }

    public void addShift(LocalDate date, ShiftType type) {
        shiftManager.addShift(date, type);
    }

    public Shift getShift(LocalDate date, ShiftType type) {
        return shiftManager.getShift(date, type);
    }

    public Shift getExistingShift(LocalDate date, ShiftType type) {
        return shiftManager.getExistingShift(date, type);
    }

    public void resetShift(Shift shift) {
        shiftManager.resetShift(shift);
    }

    public void setRequirement(Shift shift, Role role, int count) {
        shiftManager.setRequirement(shift, role, count);
    }

    public void assignEmployee(Shift shift, Role role, int employeeId) {
        shiftManager.assignEmployee(shift, role, employeeId);
    }

    public boolean needToForceAssign(Shift shift, Role role, int employeeId) {
        return shiftManager.nobodyToAssign(shift, role)
                && roleManager.isQualified(employeeId, role);
    }

    public void forceAssign(Shift shift, Role role, int employeeId) {
        shiftManager.forceAssign(shift, role, employeeId);
    }

    // assign any existing employee for special cases
    public void manualAssign(Shift shift, Role role, int employeeId) {
        shiftManager.manualAssign(shift, role, employeeId);
    }

    public void replaceEmployee(Shift shift, int currentId, int newId) {
        shiftManager.replaceEmployee(shift, currentId, newId);
    }

    public void removeEmployee(Shift shift, int employeeId) {
        shiftManager.removeEmployee(shift, employeeId);
    }

    public void publishWeekSchedule() {
        LocalDate thisSunday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        shiftManager.publishWeekSchedule(thisSunday);
    }

    public void publishNextWeekSchedule() {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftManager.publishWeekSchedule(nextSunday);
    }

    public void publishLastWeekSchedule() {
        LocalDate lastSunday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                .minusWeeks(1);
        shiftManager.publishWeekSchedule(lastSunday);
    }

    // test function for adding past shifts (mainly for history purposes)
    public void publishWeekByDate(LocalDate date) {;
        shiftManager.publishWeekSchedule(date);
    }

    public String getAvailableEmployeesForShift(Shift shift) {
        return shiftManager.getUnassignedValid(shift);
    }

    public String displayWeekAssignments() {
        return shiftManager.displayWeekAssignments();
    }

    public String displayCurrentWeek() {
        return shiftManager.displayPublishedWeek(LocalDate.now());
    }

    public String displayNextWeek() {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return shiftManager.displayPublishedWeek(nextSunday);
    }

    public String getShiftHistory() {
        return shiftManager.ShiftHistory();
    }

    public String getShiftDetails(Shift shift) {
        return shiftManager.getShiftDetails(shift);
    }

    public String getEmployeeShifts(int id) {
        return shiftManager.employeeWeekDisplay(id, LocalDate.now());
    }

    public String getNextWeekEmployeeShifts(int id) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return shiftManager.employeeWeekDisplay(id, nextSunday);
    }

    public WeekStatus getWeekStatus() {
        return shiftManager.getWeekStatus(shiftManager.getNextWeek().getStartOfWeek());
    }

    public void updateExtraHours(Shift shift, int empID, int hours) {
        if (hours < 0 || hours > 4) {
            throw new IllegalArgumentException("The number of hours off must be between 0 and 4.");
        }
        shiftManager.updateExtraHours(shift, empID, hours);
    }
}