package dev.Workers.service;

import dev.Workers.domain.PreferenceHandler;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Preference;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Manages all employees' preferences in the system.
 *
 * This class is implemented as a Singleton.
 * It allows updating preferences, checking availability,
 * and managing deadlines for preference submissions.
 */
public class PreferenceService {
    private static PreferenceHandler preferenceHandler;
    private static PreferenceService instance;

    private PreferenceService() {
        preferenceHandler = PreferenceHandler.getInstance();
    }

    public static PreferenceService getInstance() {
        if (instance == null) {
            instance = new PreferenceService();
        }
        return instance;
    }

    public Preference display(int id) {
        return preferenceHandler.getPreferences(id);
    }

    public void update(int id, DayOfWeek day, ShiftType shiftType) {
        preferenceHandler.update(id, day, shiftType);
    }

    public boolean isOnTime() {
        return preferenceHandler.isOnTime(LocalDate.now());
    }

    public DayOfWeek getDeadline() {
        return preferenceHandler.getDeadline();
    }

    public void setDeadline(DayOfWeek deadline) { preferenceHandler.setDeadline(deadline); }

    public void initPreferences(int id) {
        preferenceHandler.initPreferences(id);
    }
}