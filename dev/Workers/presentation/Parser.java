package dev.Workers.presentation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class Parser {
    public static LocalDate stringToDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date;
    }

    public static LocalDate getDateOfThisWeekFromNumber(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 7) {
            throw new IllegalArgumentException("Day number must be between 1 and 7");
        }

        // israeli week starts at sunday
        if (dayNumber == 1)
            dayNumber = 7;
        else
            dayNumber = dayNumber - 1;

        DayOfWeek dow = DayOfWeek.of(dayNumber);
        LocalDate today = LocalDate.now();
        DayOfWeek targetDay = dow;
        return today.with(TemporalAdjusters.next(targetDay));
    }


    public static LocalDate getDateOfNextWeekFromNumber(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 7) {
            throw new IllegalArgumentException("Day number must be between 1 and 7");
        }

        // israeli week starts at sunday
        DayOfWeek targetDay = (dayNumber == 1) ? DayOfWeek.SUNDAY : DayOfWeek.of(dayNumber - 1);

        LocalDate nextSunday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        // Use nextOrSame so that if dayNumber is 1 (Sunday), it returns that Sunday itself
        return nextSunday.with(TemporalAdjusters.nextOrSame(targetDay));
    }

    /**
     * Array representing days of the week (Sunday = 1)
     */
    private static final DayOfWeek[] days = {
            DayOfWeek.SUNDAY,
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    };

    /**
     * Converts a number (1-7) to a DayOfWeek
     *
     * @param dayNumber number representing the day (1=Sunday,...,7=Saturday)
     * @return corresponding DayOfWeek
     */
    public static DayOfWeek getDayFromNumber(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 7) {
            throw new IllegalArgumentException("Day number must be between 1 and 7");
        }
        return days[dayNumber - 1];
    }

    // public static List<Integer>(String
}
