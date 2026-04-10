package dev.Workers.domain;

import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Constraint;

import java.time.DayOfWeek;
import java.util.*;

import static dev.Workers.domain.Enums.shiftType.*;

public class ConstraintManager  {
    private Map<Integer, Constraint> employeeConstraints;

    private static ConstraintManager instance;

    private ConstraintManager() {
        this.employeeConstraints = new HashMap<>();

    }

    public static ConstraintManager getInstance() {
        if (instance == null) {
            instance = new ConstraintManager();
        }
        return instance;
    }

    public Map<Integer, Constraint> getEmployeeConstraints() {
        return employeeConstraints;
    }

    public Constraint display(int id) {
        return employeeConstraints.get(id);
    }

    public void update(int id, DayOfWeek day, shiftType shiftType) {
        Constraint employeeConstraints =display(id);
        employeeConstraints.getWeekConstraints().put(day, shiftType);
    }

    public boolean isEmployeeAvailable(int id, DayOfWeek day, shiftType shiftType) {
        return (employeeConstraints.get(id).getShiftType(day) == shiftType
                || employeeConstraints.get(id).getShiftType(day) == wholeDay)
                && shiftType != notWorking;
    }

    private static final DayOfWeek[] days = {
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    };

    public static DayOfWeek getDayFromNumber(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 7) {
            throw new IllegalArgumentException("Day must be between 1-7");
        }
        return days[dayNumber - 1];
    }

    //public static shiftType getShiftTypeFromNumber (int dayNumber) {}
}