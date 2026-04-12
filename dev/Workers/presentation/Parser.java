package dev.Workers.presentation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class Parser {
    public static LocalDate stringToDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date;
    }

    public static LocalDate dayNumberToDate(int dayNumber) {
        DayOfWeek dow = DayOfWeek.of(dayNumber);
        LocalDate today = LocalDate.now();
        DayOfWeek targetDay = dow;
        return today.with(TemporalAdjusters.next(targetDay));
    }

    // public static List<Integer>(String
}
