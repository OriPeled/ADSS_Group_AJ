package dev.Workers.domain;

import dev.Workers.domain.Enums.shiftType;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

import static dev.Workers.domain.Enums.shiftType.wholeDay;
     /**
      *  Represents employee constraints for working shifts.
     *
     *   A constraint can be defined either:
     *   1. For a full week (using weekConstraints map)
     *   2. For a specific day and shift type
     *
     *   By default, all days are initialized to wholeDay (no restriction).
     */
public class Constraint {
    // Specific day constraint
    private DayOfWeek day;
    // Specific shift type constraint for a single day
    private shiftType shiftType;
    //Weekly constraints mapping each day to allowed shift type
    private Map<DayOfWeek, shiftType> weekConstraints;
    //private boolean can;

    /**
    *  constructor.
    * Initializes all days in the week to wholeDay (no restriction).
    */
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

    /**
     *
     * @param day
     * @param shiftType
     * Sets the shift type constraint for a specific day.
     */
    public void setShiftType(DayOfWeek day, shiftType shiftType) {
        weekConstraints.put(day, shiftType);
    }
    /**
    * @return the full weekly constraints map
    */
    public Map<DayOfWeek, shiftType> getWeekConstraints() {
        return weekConstraints;
    }
    /**
     * Compares two constraints based on day and shift type.
     *
     * @param o object to compare
     * @return true if both constraints refer to the same day and shift type
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return day.equals(that.day) && shiftType == that.shiftType;
    }

         @Override
         public String toString() {
             return "Constraint{" +
                     "day=" + day +
                     ", shiftType=" + shiftType +
                     ", weekConstraints=" + weekConstraints +
                     '}';
         }
     }
