package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.shiftType;

import java.time.DayOfWeek;
import java.util.Objects;

public class Constraint {
    private DayOfWeek day;
    private shiftType shiftType;
    //private boolean can;

    public Constraint(DayOfWeek day, shiftType shiftType) {
        this.day = day;
        this.shiftType = shiftType;
        //this.can = false;
    }

    public Constraint(DayOfWeek day, String shiftTypeString) {
        this.day = day;
        this.shiftType = shiftType.valueOf(shiftTypeString);
        //this.can = false;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public shiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(shiftType shiftType) {
        this.shiftType = shiftType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return day.equals(that.day) && shiftType == that.shiftType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, shiftType);
    }
}
