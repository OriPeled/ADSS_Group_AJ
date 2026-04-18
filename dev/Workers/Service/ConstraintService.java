package dev.Workers.Service;

import dev.Workers.domain.ConstraintManager;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Constraint;

import java.time.DayOfWeek;
import java.time.LocalDate;

import java.util.*;

/**
 * Manages all employees' constraints in the system.
 *
 * This class is implemented as a Singleton.
 * It allows updating constraints, checking availability,
 * and managing deadlines for constraint submissions.
 */
public class ConstraintService {
    private static final ConstraintManager constraintManager = ConstraintManager.getInstance();
    private static ConstraintService instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private ConstraintService() {
    }

    public static ConstraintService getInstance() {
        if (instance == null) {
            instance = new ConstraintService();
        }
        return instance;
    }

    /*public void setNextThursdayDeadline() {
        constraintManager.setNextWeekDeadline();
    }

    public void setThisThursdayDeadline() {
        constraintManager.setThisWeekDeadline();
    }*/

    /**
     * @return map of all employee constraints
     */
    public Map<Integer, Constraint> getEmployeeConstraints() {
        return constraintManager.getConstraintsByID();
    }

    /**
     * Returns constraints of a specific employee
     *
     * @param id employee ID
     * @return Constraint object
     */
    public Constraint display(int id) {
        return constraintManager.getConstraints(id);
    }

    /**
     * Updates constraint for a specific employee and day 
     *
     * @param id employee ID
     * @param day day of week
     * @param shiftType desired shift type
     */
    public void update(int id, DayOfWeek day, ShiftType shiftType) {
        DayOfWeek deadline = DayOfWeek.from(constraintManager.getDeadline());
       /* if (deadline != null && isOnTime()) {
            throw new RuntimeException("Submission failed: The deadline for submitting constraints (" + deadline + ") has passed.");
        }*/
        constraintManager.update(id, day, shiftType);
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
        return constraintManager.isEmployeeAvailable(id, day, shiftType);
    }
    /**
     * Array representing days of the week (Sunday = 1)
     */
    private static final DayOfWeek[] days = {
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    };

    /**
     * Converts a number (1-7) to a DayOfWeek
     *
     * @param dayNumber number representing the day (1=Sunday,...,7=Saturday)
     * @return corresponding DayOfWeek
     */
   /*public static DayOfWeek getDayFromNumber(int dayNumber) {
        return Parser.getDayFromNumber(dayNumber);
    }*/


    /**
     * Checks if current date is before deadline
     *
     * @return true if still before deadline, false otherwise
     */
    public boolean isOnTime() {
        return constraintManager.isOnTime(LocalDate.now());
    }
    /**
     * @return deadline for updating constraints
     */
    public DayOfWeek getDeadline() {
        return constraintManager.getDeadline();
    }
    /**
     * Sets deadline for updating constraints
     *
     * @param deadline new deadline
     */
    public void setDeadline(DayOfWeek deadline) {
        constraintManager.setDeadline(deadline);
     }
    /**
     * Resets all employees' constraints.
     *
     * For every employee:
     * - All days in the week will be set to wholeDay
     */
    public void resetAllConstraints() {
        constraintManager.resetAllConstraints();
    }

    public void initConstraintForEmployee(int id) {
        constraintManager.initConstraintForEmployee(id);
    }
}