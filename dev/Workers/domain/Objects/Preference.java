package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.ShiftType;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static dev.Workers.domain.Enums.ShiftType.ANY;
     /**
      *  Represents employee preferences for working shifts.
     *
     *   A constraint can be defined either:
     *   1. For a full week (using weekPreferences map)
     *   2. For a specific day and shift type
     *
     *   By default, all days are initialized to wholeDay (no restriction).
     */
public class Preference {
    private DayOfWeek day;                              // Specific day preference
    private ShiftType shiftType;                        // Specific shift type preference for a single day
    private Map<DayOfWeek, ShiftType> weekPreferences;  // weekly preferences to allowed shift type

    /**
    *  constructor.
    * Initializes all days in the week to wholeDay (no restriction).
    */
    public Preference() {
        this.weekPreferences = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) {
            weekPreferences.put(d, ANY);
        }
    }

    public Preference(DayOfWeek day, String shiftTypeString) {
        this.day = day;
        this.shiftType = shiftType.valueOf(shiftTypeString);
    }

    public ShiftType getShiftType(DayOfWeek day) {
        return weekPreferences.get(day);
    }

    public void setShiftType(DayOfWeek day, ShiftType shiftType) {
        weekPreferences.put(day, shiftType);
    }

    public Map<DayOfWeek, ShiftType> getWeekPreferences() {
        return weekPreferences;
    }

         /**
          * Compares two preferences based on their full weekly preferences map.
          *
          * @param o object to compare
          * @return true if both preferences have the same weekly preferences
          */
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
             Preference that = (Preference) o;
             return Objects.equals(weekPreferences, that.weekPreferences);
         }

         @Override
         public int hashCode() {
             return Objects.hash(weekPreferences);
         }

     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder("=== My Week Preferences ===\n");

         DayOfWeek[] orderedDays = {
                 DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                 DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
         };

         int columnWidth = 30;

         // Loop 4 times (for the 4 rows needed to cover all 7 days)
         for (int i = 0; i < 4; i++) {
             // Column 1: Sunday through Wednesday (Indices 0, 1, 2, 3)
             String dayLeft = formatDay(orderedDays[i]);
             String shiftLeft = weekPreferences.get(orderedDays[i]).toString();
             String leftEntry = dayLeft + " - " + shiftLeft;

             sb.append(String.format("%-" + columnWidth + "s", leftEntry));

             // Column 2: Thursday through Saturday (Indices 4, 5, 6)
             if (i + 4 < orderedDays.length) {
                 String dayRight = formatDay(orderedDays[i + 4]);
                 String shiftRight = weekPreferences.get(orderedDays[i + 4]).toString();
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
