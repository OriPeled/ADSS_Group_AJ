package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.ShiftType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public class Shift {
    private LocalDate date;
    private ShiftType type;
    // private Time hours
    private boolean hasManager;

    public boolean hasManager() {
        return hasManager;
    }

    public void setManaged(boolean hasManager) {
        this.hasManager = hasManager;
    }

    public Shift(LocalDate date, ShiftType type) {
        this.type = type;
        // if morning => hours = 6:00-14:00, else => hours = 14:00-22:00
        this.date = date;
        this.hasManager = false;
    }

    public ShiftType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getShiftDay() { return date.getDayOfWeek(); }

    /**
     *
     * @param o
     * @return true if date and shift tye equal ,otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shift shift = (Shift) o;
        return Objects.equals(date, shift.date) && type == shift.type;
    }

    @Override
    public int hashCode() {
        // Generate a hash based on the data fields
        return Objects.hash(date, type);
    }

    @Override
    public String toString() {
        String managerIndicator = hasManager ? "[Managed]" : "[Unmanaged]";
        return String.format("%s (%s) %s", date, type, managerIndicator);
    }

    public String toStringByWeekDay() { return date.getDayOfWeek().name() + " (" + type + ")"; }
}
