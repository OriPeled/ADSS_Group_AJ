package dev.Workers.utils;

import dev.Workers.domain.Enums.LicenseType;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

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

    public static LicenseType stringToLicenseType(String licenseString) {
        if (licenseString == null || licenseString.trim().isEmpty()) {
            throw new IllegalArgumentException("License string cannot be null or empty.");
        }

        try {
            // can type "a", "A", " a "
            return LicenseType.valueOf(licenseString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid license type: '" + licenseString + "'.");
        }
    }

    public static Map<LicenseType, Integer> stringToDriverReqs(String reqsString) {
        Map<LicenseType, Integer> driverReqs = new HashMap<>();

        if (reqsString == null || reqsString.trim().isEmpty()) {
            return driverReqs;
        }

        String[] pairs = reqsString.split(",");

        for (String pair : pairs) {
            String[] parts = pair.split(":");

            if (parts.length == 2) {
                try {
                    // Use your shiny new parser here!
                    LicenseType licenseType = stringToLicenseType(parts[0]);
                    int count = Integer.parseInt(parts[1].trim());

                    driverReqs.put(licenseType, count);

                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping invalid requirement format: '" + pair + "'");
                }
            }
        }

        return driverReqs;
    }
}
