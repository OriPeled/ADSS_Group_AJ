package dev.Workers.domain;

import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Preference;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static dev.Workers.domain.Enums.ShiftType.*;

/**
 * Manages all employees' preferences in the system.
 *
 * This class is implemented as a Singleton.
 * It allows updating preferences, checking availability,
 * and managing deadlines for preference submissions.
 */
public class PreferenceHandler {
    private DayOfWeek deadline = DayOfWeek.THURSDAY;    // deadline for submitting/updating preferences
    private Map<Integer, Preference> preferences;   // employee ID to employee week preferences

    private EmployeeHandler employeeHandler = EmployeeHandler.getInstance();

    private static PreferenceHandler instance;

    private PreferenceHandler() {
        this.preferences = new HashMap<>();
    }

    public static PreferenceHandler getInstance() {
        if (instance == null) {
            instance = new PreferenceHandler();
        }
        return instance;
    }

    public Map<Integer, Preference> getAllPreferences() {
        return preferences;
    }

    // received type is always morning/evening
    public void extendPreferences(int empId, DayOfWeek day, ShiftType type) {
        ShiftType empDayPreferences = getPreferences(empId).getShiftType(day);
        if (empDayPreferences == REST)
            getPreferences(empId).setShiftType(day, type);
        else if (empDayPreferences == MORNING || empDayPreferences == EVENING)
            getPreferences(empId).setShiftType(day, ANY);
    }

    public Preference getPreferences(int id) {
        return preferences.get(id);
    }

    /**
     * Updates the preference for a specific employee and day.
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
     * Updates the preference for a specific employee and day, given a specific current date.
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
                    "Submission failed: The deadline for submitting preferences (" + deadline + ") has passed."
            );
        }

        Preference employeePreferences = getPreferences(id);
        employeePreferences.getWeekPreferences().put(day, shiftType);
    }

    public boolean isEmployeeAvailable(int id, DayOfWeek day, ShiftType shiftType) {
        return (preferences.get(id).getShiftType(day) == shiftType
                || preferences.get(id).getShiftType(day) == ANY)
                && shiftType != REST;
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

    public DayOfWeek getDeadline() {
        return deadline;
    }

    public void setDeadline(DayOfWeek deadline) { this.deadline = deadline; }

    /**
     * Resets all employees' preferences.
     *
     * For every employee:
     * - All days in the week will be set to 'any',
     * this happens right after the HR admin publishes the week schedule.
     */
    public void resetAllPreferences() {
        for (Integer id : preferences.keySet()) {
            initPreferences(id);
        }
    }

    public void initPreferences(int id) {
        preferences.put(id, new Preference());

        DayOfWeek dayOff = employeeHandler.getEmployee(id).getTerms().getDayOff();
        preferences.get(id).setShiftType(dayOff, REST);
    }
}