package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.ShiftType;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;

import static dev.Workers.domain.Enums.ShiftType.any;
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
    private ShiftType shiftType;
    //Weekly constraints mapping each day to allowed shift type
    private Map<DayOfWeek, ShiftType> weekConstraints;
    //private Map<DayOfWeek, shiftType> weekConstraints = new EnumMap<>(DayOfWeek.class);
    //private boolean can;

    /**
    *  constructor.
    * Initializes all days in the week to wholeDay (no restriction).
    */
    public Constraint() {
        this.weekConstraints = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) {
            weekConstraints.put(d, any);
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
    public ShiftType getShiftType(DayOfWeek day) {
        return weekConstraints.get(day);
    }

    /**
     *
     * @param day
     * @param shiftType
     * Sets the shift type constraint for a specific day.
     */
    public void setShiftType(DayOfWeek day, ShiftType shiftType) {
        weekConstraints.put(day, shiftType);
    }
    /**
    * @return the full weekly constraints map
    */
    public Map<DayOfWeek, ShiftType> getWeekConstraints() {
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
         StringBuilder sb = new StringBuilder("=== My Week Constraints ===\n");

         // Define the custom order starting with Sunday
         DayOfWeek[] orderedDays = {
                 DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                 DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
         };

         int columnWidth = 30; // Increased width for plenty of space

         // Loop 4 times (for the 4 rows needed to cover all 7 days)
         for (int i = 0; i < 4; i++) {
             // Column 1: Sunday through Wednesday (Indices 0, 1, 2, 3)
             String dayLeft = formatDay(orderedDays[i]);
             String shiftLeft = weekConstraints.get(orderedDays[i]).toString();
             String leftEntry = dayLeft + " - " + shiftLeft;

             sb.append(String.format("%-" + columnWidth + "s", leftEntry));

             // Column 2: Thursday through Saturday (Indices 4, 5, 6)
             if (i + 4 < orderedDays.length) {
                 String dayRight = formatDay(orderedDays[i + 4]);
                 String shiftRight = weekConstraints.get(orderedDays[i + 4]).toString();
                 sb.append(dayRight).append(" - ").append(shiftRight);
             }

             sb.append("\n");
         }

         return sb.toString();
     }

     // Helper to make "SUNDAY" into "Sunday"
     private String formatDay(DayOfWeek d) {
         String name = d.toString().toLowerCase();
         return name.substring(0, 1).toUpperCase() + name.substring(1);
     }
 }
