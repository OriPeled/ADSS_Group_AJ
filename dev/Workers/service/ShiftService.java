package dev.Workers.service;

import dev.Workers.Service.RoleService;
import dev.Workers.domain.EmployeeHandler;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.ShiftHandler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;

/**
 * ShiftService acts as the entry point for UI/Web controllers.
 * It delegates all state management and business rules to the ShiftManager.
 */
public class ShiftService {
    private static ShiftHandler shiftHandler;
    private static AssignmentService assignmentService;
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
        assignmentService = AssignmentService.getInstance();
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

    public void removeEmployee(Shift shift, int employeeId) {
        shiftHandler.removeEmployee(shift, employeeId);
    }

    public void publishNextWeekSchedule(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftHandler.publishWeekSchedule(branch, nextSunday);
    }

    public void forcePublishNextWeekSchedule(Branch branch) {
        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        shiftHandler.forcePublishWeekSchedule(branch, nextSunday);
    }


    // test function for adding past shifts (mainly for history purposes)
    public void publishWeekByDate(Branch branch, LocalDate date) {;
        shiftHandler.publishWeekSchedule(branch, date);
    }

    public boolean isShiftAssigned(Shift shift) {
        return shiftHandler.isShiftAssigned(shift);
    }

    public String getPotentialEmployees(Shift shift) {
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

    public void updateExtraHoursManually(Shift shift, int empID, int hours) {
        if (hours < 0 || hours > 4) {
            throw new IllegalArgumentException("The number of hours off must be between 0 and 4.");
        }
        shiftHandler.updateExtraHoursManually(shift, empID, hours);
    }

    // For Assignments
    public void sendRequest(Shift shift, Role role, int empId) {
        assignmentService.sendRequest(shift, role, empId);
    }

    // For Replacements
    public void sendRequest(Shift shift, int curId, int newId) {
        assignmentService.sendRequest(shift, curId, newId);
    }

    public boolean assignmentNeedsApproval(int employeeId) {
        return assignmentService.assignmentNeedsApproval(employeeId);
    }

    public String displayNextPendingAssignment(int employeeId) {
        return assignmentService.displayNextPendingAssignment(employeeId);
    }

    public void processRequest(int employeeId, boolean isApproved) {
        shiftHandler.processRequest(employeeId, isApproved);
    }

    public List<String> popRequestAnswers(Branch branch) {
        return assignmentService.popRequestAnswers(branch);
    }

    public void initShiftsWeek(Branch branch) {
        shiftHandler.initShiftsWeek(branch);
    }

    public boolean pendingRequestsLeft() {
        return assignmentService.pendingRequestsLeft();
    }

    // used by TP module
    public Set<Integer> getShiftDrivers(Branch branch, LocalDate shiftDate, LocalTime startTime, LocalTime endTime) {
        return shiftHandler.getShiftDrivers(branch, shiftDate, startTime, endTime);
    }
}