package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.ShiftType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public class Shift {
    //date of shift
    private LocalDate shiftDate;
    // type of the shift
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
    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shift)) return false;

        Shift shift1 = (Shift) o;

        return shiftDate.equals(shift1.shiftDate)
                && type == shift1.type;
    }
    public int compareTo(Shift other) {
        int cmp = this.shiftDate.compareTo(other.shiftDate);
        if (cmp != 0) return cmp;
        return this.type.compareTo(other.type);
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Same memory address
        if (o == null || getClass() != o.getClass()) return false; // Not the same class
        Shift shift = (Shift) o;
        // Compare the actual data fields
        return Objects.equals(shiftDate, shift.shiftDate) && type == shift.type;
    }

    @Override
    public int hashCode() {
        // Generate a hash based on the data fields
        return Objects.hash(shiftDate, type);
    }
    /*@Override
    public int hashCode() {
        return Objects.hash(getShiftDate(), getType());
    }*/

    @Override
    public String toString() {
        return shiftDate + " - " + type;
    }

    public String toStringByWeekDay() { return shiftDate.getDayOfWeek().name() + " (" + type + ")"; }
}
