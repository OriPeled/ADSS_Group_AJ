package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.ShiftType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public class Shift {
    private LocalDate shiftDate;
    private ShiftType type;

    public Shift(LocalDate shiftDate, ShiftType type) {
        this.type = type;
        this.shiftDate = shiftDate;
    }

    public ShiftType getType() {
        return type;
    }
    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public DayOfWeek getShiftDay() { return shiftDate.getDayOfWeek(); }

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
        return Objects.equals(shiftDate, shift.shiftDate) && type == shift.type;
    }

    @Override
    public int hashCode() {
        // Generate a hash based on the data fields
        return Objects.hash(shiftDate, type);
    }

    @Override
    public String toString() {
        return shiftDate + " - " + type;
    }

    public String toStringByWeekDay() { return shiftDate.getDayOfWeek().name() + " (" + type + ")"; }
}
