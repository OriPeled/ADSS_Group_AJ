package dev.domain;

public class Constraint {


    private Day day;
    private shiftType shiftType;


    public Constraint(Day day, shiftType shiftType) {
        this.day = day;
        this.shiftType = shiftType;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public shiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(shiftType shiftType) {
        this.shiftType = shiftType;
    }
}
