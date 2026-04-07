package dev.Workers.domain;

import java.time.LocalDate;
import java.util.Date;

public class Shift {
    private LocalDate shiftDate;
    private shiftType shift;

    public Shift(LocalDate shiftDate, shiftType shift) {
        this.shiftDate = shiftDate;
        this.shift = shift;
    }

    public Shift(LocalDate shiftDate, String shiftString) {
        this.shiftDate = shiftDate;

        shiftType shift = shiftType.valueOf(shiftString);
        this.shift = shift;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public shiftType getShift() {
        return shift;
    }

    public void setShift(shiftType shift) {
        this.shift = shift;
    }
}
