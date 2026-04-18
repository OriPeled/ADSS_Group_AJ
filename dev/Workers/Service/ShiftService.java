package dev.Workers.Service;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.ShiftResponse;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.ShiftManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

/**
 * ShiftService acts as the entry point for UI/Web controllers.
 * It delegates all state management and business rules to the ShiftManager.
 */
public class ShiftService {

    private final ShiftManager shiftManager;
    private static ShiftService instance;


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
        // The service references the single source of truth: the domain manager
        this.shiftManager = ShiftManager.getInstance();
    }

    // --- Shift Management ---

    public void addShift(LocalDate date, ShiftType type) {
        shiftManager.addShift(date, type);
    }

    public Shift getShift(LocalDate date, ShiftType type) {
        return shiftManager.getShift(date, type);
    }

    public void removeShift(Shift shift) {
        shiftManager.removeShift(shift);
    }

    public void setRequirement(Shift shift, Role role, int count) {
        shiftManager.setRequirement(shift, role, count);
    }

    public int getLeftToAssign(Shift shift, Role role) {
        return shiftManager.leftToAssign(shift, role);
    }

    public void assignEmployee(Shift shift, Role role, int employeeId) {

        ShiftResponse response = shiftManager.assignEmployee(shift, role, employeeId);

        if (response == ShiftResponse.notValid) {
            throw new RuntimeException("NOT_VALID: Employee " + employeeId +
                    " cannot be assigned to shift " + shift +
                    " for role " + role);
        } else if (response == ShiftResponse.special) {
            throw new RuntimeException("Employee constraints don't match. Special approval needed. Are you sure you want to preceed?");

        }
    }



    public void forceAssignEmployee(Shift shift, Role role, int employeeId) {
        shiftManager.forceAssign(shift, role, employeeId);
    }
    public void removeEmployeeFromShift(Shift shift, Role role, int employeeId) {
        shiftManager.removeEmployee(shift, role, employeeId);
    }

    public void replaceEmployee(Shift shift, int currentId, int newId) {
        shiftManager.replaceEmployee(shift, currentId, newId);
    }

    public void publishWeekSchedule() {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftManager.publishWeekSchedule(nextSunday);
    }

    // --- Queries & Reports ---

    public String getAvailableEmployeesForShift(Shift shift) {
        return shiftManager.getUnassignedValid(shift);
    }

    public Map<Shift, String> getWeekAssignmentStatus() {
        return shiftManager.weekAssignment();
    }

    public String displayWeekAssignments() {
        return shiftManager.displayWeekAssignments();
    }

    public String getShiftHistory() {
        return shiftManager.ShiftHistory();
    }

    public String getShiftDetails(Shift shift) {
        return shiftManager.getShiftDetails(shift);
    }

    public String getEmployeeShifts(int id) {
        return shiftManager.getEmployeeWeekDisplay(id, LocalDate.now());
    }

    public String getNextWeekEmployeeShifts(int id) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return shiftManager.getEmployeeWeekDisplay(id, nextSunday);
    }
    public boolean isRoleNeeded(Shift shift, Role role) {

        return shiftManager.isNeeded(shift, role);
    }

    public WeekStatus getWeekStatus() {
        return shiftManager.getWeekStatus(shiftManager.getNextWeek().getStartOfWeek());
    }
}