package dev.Workers.setup;

import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Objects.Branch;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TransportModule {
    // assuming TP holds Branch class
    public static Map<LicenseType, Integer> getDriverRequirements(Branch branch, LocalDate date,
                                                                  LocalTime shiftStartTime, LocalTime shiftEndTime) {
        Map<LicenseType, Integer> driverReqs = new HashMap<>();
        driverReqs.put(LicenseType.A, 2);
        driverReqs.put(LicenseType.C, 3);
        driverReqs.put(LicenseType.D, 1);
        return driverReqs;
    }

    public static int getStorekeeperRequirements(Branch branch, LocalDate date,
                                                    LocalTime shiftStartTime, LocalTime shiftEndTime) {
        return 2;
    }
}
