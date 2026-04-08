package dev.Workers.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Parser {
    public static LocalDate stringToDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date;
    }

    // public static List<Integer>(String
}
