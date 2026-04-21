package dev.Workers.Service;

import dev.Workers.domain.ConstraintManager;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Constraint;

import java.time.DayOfWeek;
import java.time.LocalDate;

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
        constraintManager.update(id, day, shiftType);
    }

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

    public void initConstraintForEmployee(int id) {
        constraintManager.initConstraintsForEmployee(id);
    }
}