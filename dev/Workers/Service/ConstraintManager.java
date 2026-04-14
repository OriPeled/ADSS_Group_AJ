package dev.Workers.Service;

import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Constraint;

import java.time.DayOfWeek;
import java.time.LocalDate;

import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static dev.Workers.domain.Enums.shiftType.*;
/**
 * Manages all employees' constraints in the system.
 *
 * This class is implemented as a Singleton.
 * It allows updating constraints, checking availability,
 * and managing deadlines for constraint submissions.
 */
public class ConstraintManager {
    // deadline for submitting/updating constraints
    private LocalDate deadline;
    // maps employee ID to their constraints
    private Map<Integer, Constraint> employeeConstraints;

    private static ConstraintManager instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private ConstraintManager() {
        this.employeeConstraints = new HashMap<>();
        setNextThursdayDeadline();//defult deadline is thursday
    }

    public void setNextThursdayDeadline() {
        this.deadline = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
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
    public Map<Integer, Constraint> getEmployeeConstraints() {
        return employeeConstraints;
    }

    /**
     * Returns constraints of a specific employee
     *
     * @param id employee ID
     * @return Constraint object
     */
    public Constraint display(int id) {
        return employeeConstraints.get(id);
    }

    /**
     * Updates constraint for a specific employee and day 
     *
     * @param id employee ID
     * @param day day of week
     * @param shiftType desired shift type
     */
    public void update(int id, DayOfWeek day, shiftType shiftType) {
        if (this.deadline != null && !isOnTime(LocalDate.now())) {
            throw new RuntimeException("Cannot update constraints after deadline");
        }
        Constraint employeeConstraints =display(id);
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
    public boolean isEmployeeAvailable(int id, DayOfWeek day, shiftType shiftType) {
        return (employeeConstraints.get(id).getShiftType(day) == shiftType
                || employeeConstraints.get(id).getShiftType(day) == wholeDay)
                && shiftType != notWorking;
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
    public static DayOfWeek getDayFromNumber(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 7) {
            throw new IllegalArgumentException("Day must be between 1-7");
        }
        return days[dayNumber - 1];
    }
    /**
     * Checks if current date is before deadline
     *
     * @param date date to check
     * @return true if still before deadline, false otherwise
     */
    public boolean isOnTime(LocalDate date) {
        return date.isBefore(this.deadline);
    }
    /**
     * @return deadline for updating constraints
     */
    public LocalDate getDeadline() {
        return deadline;
    }
    /**
     * Sets deadline for updating constraints
     *
     * @param deadline new deadline
     */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    /**
     * Resets all employees' constraints.
     *
     * For every employee:
     * - All days in the week will be set to wholeDay
     */
    public void resetAllConstraints() {
        for (Constraint constraint : employeeConstraints.values()) {
            // Reset each day in the week to default (wholeDay)
            for (DayOfWeek day : DayOfWeek.values()) {
                constraint.setShiftType(day, wholeDay);
            }
        }

        System.out.println("All constraints have been reset.");
    }

}