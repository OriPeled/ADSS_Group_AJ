package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.shiftType;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static dev.Workers.domain.Enums.shiftType.wholeDay;

public class Constraint {
    private DayOfWeek day;
    private shiftType shiftType;
    private Map<DayOfWeek, shiftType> weekConstraints;
    //private boolean can;

    public Constraint() {
       this.weekConstraints = new HashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            weekConstraints.put(d, wholeDay);
        }
    }

    public Constraint(DayOfWeek day, String shiftTypeString) {
        this.day = day;
        this.shiftType = shiftType.valueOf(shiftTypeString);
        //this.can = false;
    }

    /**
     *
     * @param day
     * @return type of shift
     */
    public shiftType getShiftType(DayOfWeek day) {
        return weekConstraints.get(day);
    }

    public void setShiftType(DayOfWeek day, shiftType shiftType) {
        weekConstraints.put(day, shiftType);
    }

    public Map<DayOfWeek, shiftType> getWeekConstraints() {
        return weekConstraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return day.equals(that.day) && shiftType == that.shiftType;
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
}
