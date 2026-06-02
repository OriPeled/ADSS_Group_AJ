package dev.Workers.Service;

import dev.Workers.domain.ConstraintHandler;
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
    private static ConstraintHandler constraintHandler;
    private static ConstraintService instance;

    private ConstraintService() {
        constraintHandler = ConstraintHandler.getInstance();
    }

    public static ConstraintService getInstance() {
        if (instance == null) {
            instance = new ConstraintService();
        }
        return instance;
    }

    public Constraint display(int id) {
        return constraintHandler.getConstraints(id);
    }

    public void update(int id, DayOfWeek day, ShiftType shiftType) {
        constraintHandler.update(id, day, shiftType);
    }

    public boolean isOnTime() {
        return constraintHandler.isOnTime(LocalDate.now());
    }

    public DayOfWeek getDeadline() {
        return constraintHandler.getDeadline();
    }

    public void setDeadline(DayOfWeek deadline) { constraintHandler.setDeadline(deadline); }

    public void initConstraintForEmployee(int id) {
        constraintHandler.initConstraintsForEmployee(id);
    }
}