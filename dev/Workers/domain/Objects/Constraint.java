package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.shiftType;

import java.time.LocalDate;

public class Constraint {
    private LocalDate date;
    private dev.Workers.domain.Enums.shiftType shiftType;
    //private boolean can;

    public Constraint(LocalDate date, shiftType shiftType) {
        this.date = date;
        this.shiftType = shiftType;
        //this.can = false;
    }

    public Constraint(LocalDate date, String shiftTypeString) {
        this.date = date;
        this.shiftType = shiftType.valueOf(shiftTypeString);
        //this.can = false;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setdate(LocalDate date) {
        this.date = date;
    }

    public shiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(shiftType shiftType) {
        this.shiftType = shiftType;
    }

}
