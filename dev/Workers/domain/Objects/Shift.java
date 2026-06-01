package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.ShiftType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static dev.Workers.domain.Enums.ShiftType.EVENING;
import static dev.Workers.domain.Enums.ShiftType.MORNING;

public class Shift {
    private Branch branch;
    private LocalDate date;
    private ShiftType type;
    private boolean hasManager;
    private LocalTime startTime;
    private LocalTime endTime;

    public boolean hasManager() {
        return hasManager;
    }

    public void setManaged(boolean hasManager) {
        this.hasManager = hasManager;
    }

    public Shift(Branch branch, LocalDate date, ShiftType type) {
        this.branch = branch;
        this.type = type;
        if (type == MORNING) {
            startTime = LocalTime.of(6, 0);
            endTime = LocalTime.of(14, 0);
        }
        else if (type == EVENING) {
            startTime = LocalTime.of(14, 0);
            endTime = LocalTime.of(22, 0);
        }

        this.date = date;
        this.hasManager = false;
    }

    /*public Shift(LocalDate date, LocalTime startTime, LocalTime endTime) {      // Branch branch,
        //this.branch = branch;
        if (startTime == LocalTime.of(6, 0) && endTime == LocalTime.of(14, 0))
            type = MORNING;
        else if (startTime == LocalTime.of(14, 0) && endTime == LocalTime.of(22, 0))
            type = EVENING;
        else {
            throw new IllegalArgumentException("Invalid shift hours.");
        }

        this.date = date;
        this.hasManager = false;
    }*/

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public ShiftType getType() {
        return type;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getShiftDay() { return date.getDayOfWeek(); }

    public String getShiftHours() {
        return startTime + " - " + endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    // Return the end time as a proper time object
    public LocalTime getEndTime() {
        return endTime;
    }

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
        return date.equals(shift.date) &&
                type == shift.type &&
                branch.equals(shift.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branch, date, type);
    }

    @Override
    public String toString() {
        String managerIndicator = hasManager ? "[Managed]" : "[Unmanaged]";
        return String.format("%s (%s) %s", date, type, managerIndicator);
    }

    public String toStringByWeekDay() { return date.getDayOfWeek().name() + " (" + type + ")"; }
}
