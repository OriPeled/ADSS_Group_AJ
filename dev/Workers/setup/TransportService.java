package dev.Workers.setup;

import java.time.LocalDate;
import java.time.LocalTime;

public class TransportService {
    public static String getDriverRequirements(String branch, LocalDate date,
                                                                  LocalTime shiftStartTime, LocalTime shiftEndTime) {
        return "A: 2, B: 0, C: 3, D: 1";
    }

    public static int getStorekeeperRequirements(String branch, LocalDate date,
                                                    LocalTime shiftStartTime, LocalTime shiftEndTime) {
        return 2;
    }
}
