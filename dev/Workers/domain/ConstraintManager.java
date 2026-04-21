package dev.Workers.domain;

import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Constraint;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
    private DayOfWeek deadline = DayOfWeek.THURSDAY;    // deadline for submitting/updating constraints
    private Map<Integer, Constraint> constraintsByID;   // employee ID to employee week constraints

    private static ConstraintManager instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private ConstraintManager() {
        this.constraintsByID = new HashMap<>();
    }

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
     * Updates the constraint for a specific employee and day.
     * This method acts as a wrapper that uses the current system date,
     * ensuring existing code that calls it remains unbroken.
     *
     * @param id        the employee ID
     * @param day       the day of the week to update
     * @param shiftType the desired shift type
     * @throws RuntimeException if the submission deadline has passed
     */
    public void update(int id, DayOfWeek day, ShiftType shiftType) {
        // Delegate to the overloaded method using the actual current date
        update(id, day, shiftType, LocalDate.now());
    }

    /**
     * Updates the constraint for a specific employee and day, given a specific current date.
     * This overloaded method allows for Dependency Injection of the date,
     * which is crucial for deterministic and reliable unit testing.
     *
     * @param id          the employee ID
     * @param day         the day of the week to update
     * @param shiftType   the desired shift type
     * @param currentDate the date to be considered as "today" for deadline evaluation
     * @throws RuntimeException if the submission deadline has passed
     */
    public void update(int id, DayOfWeek day, ShiftType shiftType, LocalDate currentDate) {
        DayOfWeek deadline = getDeadline();

        if (deadline != null && !isOnTime(currentDate)) {
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
     * Helper method to convert a standard Java DayOfWeek into the Israeli week format.
     * In the default ISO-8601 standard (used by Java), Monday is 1 and Sunday is 7.
     * This method adjusts the values so that the Israeli work week starts on Sunday.
     *
     * @param day the standard DayOfWeek enum value to convert.
     * @return an integer representing the day in the Israeli week (Sunday = 1, Monday = 2, ..., Saturday = 7).
     */
    private int getIsraeliDayValue(DayOfWeek day) {
        if (day == DayOfWeek.SUNDAY) {
            return 1;
        }
        // Since DayOfWeek.MONDAY has a value of 1, adding 1 shifts it correctly for the rest of the week.
        return day.getValue() + 1;
    }

    /**
     * Checks if a given date is strictly before the configured submission deadline.
     * The comparison relies on the Israeli week structure to ensure accurate logic
     * across the week's boundary (e.g., comparing Sunday to Tuesday).
     *
     * @param date the date to check against the deadline.
     * @return true if the date's day of the week is before the deadline day, false otherwise.
     */
    public boolean isOnTime(LocalDate date) {
        DayOfWeek currentDay = date.getDayOfWeek();

        return getIsraeliDayValue(currentDay) < getIsraeliDayValue(deadline);
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
     * - All days in the week will be set to 'any',
     * this happens right after the HR admin publishes the week schedule.
     */
    public void resetAllConstraints() {
        for (Constraint constraint : constraintsByID.values()) {
            for (DayOfWeek day : DayOfWeek.values()) {
                constraint.setShiftType(day, any);
            }
        }
    }

    public void initConstraintsForEmployee(int id) {
        constraintsByID.put(id, new Constraint());
    }

}