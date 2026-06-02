package dev.Workers.Service;

import dev.Workers.domain.EmployeeHandler;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.ShiftHandler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * ShiftService acts as the entry point for UI/Web controllers.
 * It delegates all state management and business rules to the ShiftManager.
 */
public class ShiftService {
    private static ShiftHandler shiftHandler;
    private static AssignmentsService assignmentsService;
    private static EmployeeHandler employeeHandler;
    private static RoleService roleService;

    private static ShiftService instance;

    public static ShiftService getInstance() {
        if (instance == null) {
            instance = new ShiftService();
        }
        return instance;
    }

    private ShiftService() {
        shiftHandler = ShiftHandler.getInstance();
        assignmentsService = AssignmentsService.getInstance();
        employeeHandler = EmployeeHandler.getInstance();
        roleService = RoleService.getInstance();
    }

    public void addShift(Branch branch, LocalDate date, ShiftType type) {
        shiftHandler.addShift(branch, date, type);
    }

    public Shift getShift(Branch branch, LocalDate date, ShiftType type) {
        return shiftHandler.getShift(branch, date, type);
    }

    public Shift getExistingShift(Branch branch, LocalDate date, ShiftType type) {
        return shiftHandler.getExistingShift(branch, date, type);
    }

    public void resetShift(Shift shift) {
        shiftHandler.resetShift(shift);
    }

    public boolean isShiftsWeekEmpty(Branch branch) {
        return shiftHandler.isShiftsWeekEmpty(branch);
    }

    public void setRequirement(Shift shift, Role role, int count) {
        shiftHandler.setRequirement(shift, role, count);
    }

    public void assignEmployee(Shift shift, Role role, int employeeId) {
        shiftHandler.assignEmployee(shift, role, employeeId);
    }

    public boolean needToForceAssign(Shift shift, Role role, int employeeId) {
        return shiftHandler.needToForceAssign(shift, role, employeeId);
    }

    public void forceAssign(Shift shift, Role role, int employeeId) {
        shiftHandler.forceAssign(shift, role, employeeId);
    }

    // assign any existing employee for special cases
    public void manualAssign(Shift shift, Role role, int employeeId) {
        shiftHandler.manualAssign(shift, role, employeeId);
    }

    public void replaceEmployee(Shift shift, int currentId, int newId) {
        shiftHandler.replaceEmployee(shift, currentId, newId);
    }

    public boolean needToForceReplace(Shift shift, int curId, int newId) {
        employeeHandler.validateEmployeeBasic(curId, shift.getDate());
        employeeHandler.validateEmployeeBasic(newId, shift.getDate());

        return shiftHandler.needToForceReplace(shift, curId, newId);
    }

    public void forceReplace(Shift shift, int curId, int newId) {
        shiftHandler.forceReplace(shift, curId, newId);
    }

    public void removeEmployee(Shift shift, int employeeId) {
        shiftHandler.removeEmployee(shift, employeeId);
    }

    public boolean hasRequests() {
        return AssignmentsService.hasRequests();
    }

    public void publishWeekSchedule(Branch branch) {
        LocalDate thisSunday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        shiftHandler.publishWeekSchedule(branch, thisSunday);
    }

    public void publishNextWeekSchedule(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftHandler.publishWeekSchedule(branch, nextSunday);
    }

    public void forcePublishNextWeekSchedule(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftHandler.forcePublishWeekSchedule(branch, nextSunday);
    }

    public void publishLastWeekSchedule(Branch branch) {
        LocalDate lastSunday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                .minusWeeks(1);
        shiftHandler.publishWeekSchedule(branch, lastSunday);
    }

    // test function for adding past shifts (mainly for history purposes)
    public void publishWeekByDate(Branch branch, LocalDate date) {;
        shiftHandler.publishWeekSchedule(branch, date);
    }

    public boolean isShiftAssigned(Shift shift) {
        return shiftHandler.isShiftAssigned(shift);
    }

    public String getAvailableEmployeesForShift(Shift shift) {
        return shiftHandler.getUnassignedValid(shift);
    }

    public String displayWeekAssignments(Branch branch) {
        return shiftHandler.displayWeekAssignments(branch);
    }

    public String displayCurrentWeek(Branch branch) {
        return shiftHandler.displayPublishedWeek(branch, LocalDate.now());
    }

    public String displayNextWeek(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return shiftHandler.displayPublishedWeek(branch, nextSunday);
    }

    public String getShiftHistory(Branch branch) {
        return shiftHandler.ShiftHistory(branch);
    }

    public String getShiftDetails(Shift shift) {
        return shiftHandler.getShiftDetails(shift);
    }

    public String getEmployeeShifts(int id) {
        return shiftHandler.employeeWeekDisplay(id, LocalDate.now());
    }

    public String getNextWeekEmployeeShifts(int id) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return shiftHandler.employeeWeekDisplay(id, nextSunday);
    }

    public WeekStatus getWeekStatus(Branch branch) {
        return shiftHandler.getWeekStatus(branch, shiftHandler.getNextWeek().getStartOfWeek());
    }

    public void updateExtraHours(Shift shift, int empID, int hours) {
        if (hours < 0 || hours > 4) {
            throw new IllegalArgumentException("The number of hours off must be between 0 and 4.");
        }
        shiftHandler.updateExtraHours(shift, empID, hours);
    }

    // For Assignments
    public void sendRequest(Shift shift, Role role, int empId) {
        assignmentsService.sendRequest(shift, role, empId);
    }

    // For Replacements
    public void sendRequest(Shift shift, int curId, int newId) {
        assignmentsService.sendRequest(shift, curId, newId);
    }

    public void approveNextAssignment(int employeeId) {
        shiftHandler.approveNextAssignment(employeeId);
    }

    public boolean assignmentNeedsApproval(int employeeId) {
        return assignmentsService.assignmentNeedsApproval(employeeId);
    }

    public String displayNextPendingAssignment(int employeeId) {
        return assignmentsService.displayNextPendingAssignment(employeeId);
    }

    public void processRequest(int employeeId, boolean isApproved) {
        shiftHandler.processRequest(employeeId, isApproved);
    }

    public List<String> popRequestAnswers(Branch branch) {
        return assignmentsService.popRequestAnswers(branch);
    }

    public void initShiftsWeek(Branch branch) {
        shiftHandler.initShiftsWeek(branch);
    }

    public boolean pendingRequestsLeft() {
        return assignmentsService.pendingRequestsLeft();
    }
}