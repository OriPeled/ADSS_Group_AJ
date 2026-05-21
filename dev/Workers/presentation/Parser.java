package dev.Workers.presentation;

import dev.Workers.domain.Enums.Role;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import static dev.Main.scanner;

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


    public static LocalDate getDateOfNextWeekFromDayNumber(int dayNumber) {
        if (dayNumber < 1 || dayNumber > 7) {
            throw new IllegalArgumentException("Invalid input. Day must be between 1 and 7.");
        }

        LocalDate nextSunday = LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SUNDAY));

        // israeli week adapt
        return nextSunday.plusDays(dayNumber - 1);
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

    public static Role getRoleFromNumber(int roleNumber) {
        while (true) {
            if (roleNumber < 1 || roleNumber > Role.values().length) {
                throw new IllegalArgumentException("Invalid role choice.");
            }
            return Role.values()[roleNumber - 1];
        }
    }

    public static int readIntSafe() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again:");
            }
        }
    }

    static double readDoubleSafe() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again:");
            }
        }
    }


}
