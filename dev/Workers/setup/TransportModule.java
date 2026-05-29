package dev.Workers.setup;

import dev.Workers.domain.Enums.LicenseType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class TransportModule {
    public static Map<LicenseType, Integer> getDriverRequirements(LocalDate date,
                                                                    LocalTime shiftStartTime, LocalTime shiftEndTime) {
        Map<LicenseType, Integer> driverReqs = new HashMap<>();
        driverReqs.put(LicenseType.A, 2);
        driverReqs.put(LicenseType.C, 3);
        driverReqs.put(LicenseType.D, 1);
        return driverReqs;
    }

    public static int getStorekeeperRequirements(LocalDate date,
                                                    LocalTime shiftStartTime, LocalTime shiftEndTime) {
        return 1;
    }
}
