package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.shiftType;

import java.time.LocalDate;

public class Shift {

    //date of shift
    private LocalDate shiftDate;
    // type of the shift
    private shiftType shift;

    public Shift(LocalDate shiftDate, shiftType shift) {
        this.shift=shift;
        this.shiftDate = shiftDate;
    }

    public shiftType getShift() {
        return shift;
    }
    public LocalDate getShiftDate() {
        return shiftDate;
    }

    /**
     *
     * @param o
     * @return true if date and shift tye equal ,otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shift)) return false;

        Shift shift1 = (Shift) o;

        return shiftDate.equals(shift1.shiftDate)
                && shift == shift1.shift;
    }
    @Override
    public String toString() {
        return shiftDate + " - " + shift;
    }
}
