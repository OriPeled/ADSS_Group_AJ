package dev.Workers.domain;

import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Constraint;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

import static dev.Workers.domain.Enums.ShiftType.rest;
import static dev.Workers.domain.Enums.ShiftType.any;
/**
 * Manages all employees' constraints in the system.
 *
 * This class is implemented as a Singleton.
 * It allows updating constraints, checking availability,
 * and managing deadlines for constraint submissions.
 */
public class ConstraintManager {
    // deadline for submitting/updating constraints
    private DayOfWeek deadline = DayOfWeek.THURSDAY;
    // maps employee ID to their constraints
    private Map<Integer, Constraint> constraintsByID;

    private static ConstraintManager instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private ConstraintManager() {
        this.constraintsByID = new HashMap<>();
        //setNextWeekDeadline();
    }

    /*public void setNextWeekDeadline() {
        this.deadline = DayOfWeek.from(LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.THURSDAY)));
    }

    public void setThisWeekDeadline() {
        this.deadline = DayOfWeek.from(LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY)));
    }*/

    /**
     * @return the single instance of Constraint Manager
     */
    public static ConstraintManager getInstance() {
        if (instance == null) {
            instance = new ConstraintManager();
        }
        return instance;
    }
    /**
     * @return map of all employee constraints
     */
    public Map<Integer, Constraint> getConstraintsByID() {
        return constraintsByID;
    }

    /**
     * Returns constraints of a specific employee
     *
     * @param id employee ID
     * @return Constraint object
     */
    public Constraint getConstraints(int id) {
        return constraintsByID.get(id);
    }

    /**
     * Updates constraint for a specific employee and day 
     *
     * @param id employee ID
     * @param day day of week
     * @param shiftType desired shift type
     */
    public void  update(int id, DayOfWeek day, ShiftType shiftType) {
        DayOfWeek deadline = getDeadline();

        if (deadline != null && !isOnTime(LocalDate.now())) {
            throw new RuntimeException(
                    "Submission failed: The deadline for submitting constraints (" + deadline + ") has passed."
            );
        }

        Constraint employeeConstraints = getConstraints(id);
        employeeConstraints.getWeekConstraints().put(day, shiftType);

    }

    /**
     * Checks if an employee is available for a given shift
     *
     * @param id employee ID
     * @param day day of week
     * @param shiftType shift type to check
     * @return true if available, false otherwise
     */
    public boolean isEmployeeAvailable(int id, DayOfWeek day, ShiftType shiftType) {
        return (constraintsByID.get(id).getShiftType(day) == shiftType
                || constraintsByID.get(id).getShiftType(day) == any)
                && shiftType != rest;
    }

    /**
     * Checks if current date is before deadline
     *
     * @param date date to check
     * @return true if still before deadline, false otherwise
     */

    public boolean isOnTime(LocalDate date) {
        DayOfWeek currentDay = date.getDayOfWeek();

        if (currentDay == DayOfWeek.SUNDAY) {
            return true;
        }

        // Rule: If it's before the deadline day, it's open.
        // Since Mon=1, Tue=2, Wed=3, and Thu=4:
        // Any value less than 4 (Thursday) is allowed.
        return currentDay.getValue() < deadline.getValue();
    }

    /**
     * @return deadline for updating constraints
     */
    public DayOfWeek getDeadline() {
        return deadline;
    }
    /**
     * Sets deadline for updating constraints
     *
     * @param deadline new deadline
     */

    public void setDeadline(DayOfWeek deadline) {
        this.deadline = deadline;
    }

    /**
     * Resets all employees' constraints.
     *
     * For every employee:
     * - All days in the week will be set to wholeDay
     */
    public void resetAllConstraints() {
        for (Constraint constraint : constraintsByID.values()) {
            // Reset each day in the week to default (wholeDay)
            for (DayOfWeek day : DayOfWeek.values()) {
                constraint.setShiftType(day, any);
            }
        }
    }

    public void initConstraintForEmployee(int id) {
        constraintsByID.put(id, new Constraint());
    }

}