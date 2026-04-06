package dev.Workers.domain;

import java.time.LocalDate;

public class Constraint {
    private LocalDate LocalDate;
    private shiftType shiftType;
    //private boolean can;

    public Constraint(LocalDate LocalDate, shiftType shiftType) {
        this.LocalDate = LocalDate;
        this.shiftType = shiftType;
        //this.can = false;
    }

    public Constraint(LocalDate LocalDate, String shiftTypeString) {
        this.LocalDate = LocalDate;

        shiftType shiftTime = shiftType.valueOf(shiftTypeString);
        this.shiftType = shiftTime;
        //this.can = false;
    }

    public LocalDate getLocalDate() {
        return LocalDate;
    }

    public void setLocalDate(LocalDate LocalDate) {
        this.LocalDate = LocalDate;
    }

    public shiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(shiftType shiftType) {
        this.shiftType = shiftType;
    }
}
