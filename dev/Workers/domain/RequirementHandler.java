package dev.Workers.domain;

import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Objects.*;
import dev.Workers.setup.TransportModule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Stores required number of employees per role for each shift.
 *
 * Structure:
 * Shift -> List of Requirements
 */
public class RequirementHandler {
    private final Map<Shift, List<Requirement>> shiftsReqs;
    private static final RoleRegistry roleRegistry = RoleRegistry.getInstance();

    private static RequirementHandler instance;

    public static RequirementHandler getInstance() {
        if (instance == null) {
            instance = new RequirementHandler();
        }
        return instance;
    }

    public RequirementHandler() {
        this.shiftsReqs = new HashMap<>();
    }

    public void defaultInit(Shift shift) {
        List<Requirement> innerList = new ArrayList<>();

        for (Role role : roleRegistry.getAllRoles()) {
            innerList.add(role.createDefaultRequirement());
        }
        shiftsReqs.put(shift, innerList);
    }

    private Requirement getRequirement(Shift shift, Role role) {
        List<Requirement> reqs = shiftsReqs.get(shift);
        if (reqs == null) return null;

        return reqs.stream()
                .filter(r -> r.getRole().equals(role))
                .findFirst()
                .orElse(null);
    }

    public void set(Shift shift, Role role, int count) {
        if (count < 0) throw new IllegalArgumentException("Count cannot be negative");

        Requirement req = getRequirement(shift, role);
        if (req == null) {
            throw new IllegalArgumentException("No such role initialized for this shift.");
        }

        if (role instanceof DriverRole) {
            throw new IllegalArgumentException("Driver requirements are set only once during init process.");
        }

        if (req.getRole().getName().equalsIgnoreCase("Storekeeper")) {
            if (count < TransportModule.getStorekeeperRequirements(shift.getBranch(), shift.getDate(),
                    shift.getStartTime(), shift.getEndTime())) {
                throw new IllegalArgumentException("Can't be set less than required by transport manager.");
            }
        }

        req.setAmount(count);
    }

    // manual or initial set given by TP manager
    public void manualSet(Shift shift, Role role, int count) {
        if (count < 0) throw new IllegalArgumentException("Count cannot be negative");

        Requirement req = getRequirement(shift, role);
        if (req == null) {
            throw new IllegalArgumentException("No such role initialized for this shift.");
        }

        req.setAmount(count);
    }

    public int countRequired(Shift shift, Role role) {
        Requirement req = getRequirement(shift, role);
        return req != null ? req.getAmount() : 0;
    }

    public void remove(Shift shift, Role role) {
        List<Requirement> reqs = shiftsReqs.get(shift);
        if (reqs == null) return;

        reqs.removeIf(r -> r.getRole().equals(role));

        if (reqs.isEmpty()) {
            shiftsReqs.remove(shift);
        }
    }

    public Map<Shift, List<Requirement>> getAll() {
        return new HashMap<>(shiftsReqs);
    }

    // TP holds LicenseType enum
    public void getDriverWeeklyReqs(Branch branch) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        for (Shift shift : shiftsReqs.keySet()) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                Map<LicenseType, Integer> licensesNeeded = TransportModule.
                        getDriverRequirements(branch, shift.getDate(), shift.getStartTime(), shift.getEndTime());

                if (licensesNeeded != null) {
                    for (Map.Entry<LicenseType, Integer> entry : licensesNeeded.entrySet()) {
                        Role driverRole = roleRegistry.getRoleByName("Driver (" + entry.getKey() + ")");
                        manualSet(shift, driverRole, entry.getValue());
                    }
                }
            }
        }
    }

    public void getStoreKeeperWeeklyReqs(Branch branch) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        Role storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        for (Shift shift : shiftsReqs.keySet()) {
            LocalDate shiftDate = shift.getDate();
            if (!shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                int amount = TransportModule.
                        getStorekeeperRequirements(branch, shift.getDate(), shift.getStartTime(), shift.getEndTime());
                manualSet(shift, storekeeperRole, amount);
            }
        }
    }

    public void initWeeklyReqs(String rolename, Branch branch, int amount) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Role cashierRole = roleRegistry.getRoleByName(rolename);

        for (Shift shift : shiftsReqs.keySet()) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                set(shift, cashierRole, amount);
            }
        }
    }

    @Override
    public String toString() {
        if (shiftsReqs.isEmpty()) {
            return "No requirements defined.";
        }

        StringBuilder result = new StringBuilder("Shift Requirements:\n");

        for (Map.Entry<Shift, List<Requirement>> shiftEntry : shiftsReqs.entrySet()) {
            Shift shift = shiftEntry.getKey();
            List<Requirement> reqs = shiftEntry.getValue();

            result.append("Shift: ").append(shift).append(" | ");

            List<String> roleStrings = new ArrayList<>();
            for (Requirement req : reqs) {
                roleStrings.add(req.getRole().getName() + ": " + req.getAmount());
            }

            result.append(String.join(", ", roleStrings)).append("\n");
        }

        return result.toString();
    }
}