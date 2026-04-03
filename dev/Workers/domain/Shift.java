package dev.Workers.domain;

import java.util.Date;

public class Shift {
    private shiftType shift;
    private Date shiftDate;

    public Shift(shiftType shift, Date shiftDate) {
        this.shift = shift;
        this.shiftDate = shiftDate;
    }

    public shiftType getShift() {
        return shift;
    }

    public void setShift(shiftType shift) {
        this.shift = shift;
    }

    public Date getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(Date shiftDate) {
        this.shiftDate = shiftDate;
    }
}
