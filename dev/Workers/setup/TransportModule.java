package dev.Workers.setup;

import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TransportModule {
    public static Map<LicenseType, Integer> getDriverRequirements(LocalDate date, ShiftType type) {
        Map<LicenseType, Integer> driverReqs = new HashMap<>();
        driverReqs.put(LicenseType.A, 2);
        driverReqs.put(LicenseType.C, 3);
        driverReqs.put(LicenseType.D, 1);
        return driverReqs;
    }

    public static int getStorekeeperRequirements(LocalDate date, ShiftType type) {
        return 1;
    }
}
