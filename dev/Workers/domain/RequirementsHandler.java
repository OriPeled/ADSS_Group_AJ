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
public class RequirementsHandler {
    private final Map<Shift, List<Requirement>> shiftsReqs;
    private static final RoleRegistry roleRegistry = RoleRegistry.getInstance();

    private static RequirementsHandler instance;

    public static RequirementsHandler getInstance() {
        if (instance == null) {
            instance = new RequirementsHandler();
        }
        return instance;
    }

    public RequirementsHandler() {
        this.shiftsReqs = new HashMap<>();
    }

    public void init(Shift shift) {
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

        if (req.getRole().getName().equalsIgnoreCase("Storekeeper")) {
            if (count < TransportModule.getStorekeeperRequirements(shift.getBranch(), shift.getDate(),
                    shift.getStartTime(), shift.getEndTime())) {
                throw new IllegalArgumentException("Can't be set less than required by transport manager.");
            }
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

    // TP holds LicenseType enum
    public void getDriverReqs(Branch branch) {
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
                        set(shift, driverRole, entry.getValue());
                    }
                }
            }
        }
    }

    public void getStoreKeeperReqs(Branch branch) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        Role storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        for (Shift shift : shiftsReqs.keySet()) {
            LocalDate shiftDate = shift.getDate();
            if (!shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                int amount = TransportModule.
                        getStorekeeperRequirements(branch, shift.getDate(), shift.getStartTime(), shift.getEndTime());
                set(shift, storekeeperRole, amount);
            }
        }
    }

    public void setCashierWeekReqs(Branch branch, int amount) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Role cashierRole = roleRegistry.getRoleByName("Cashier");

        for (Shift shift : shiftsReqs.keySet()) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                set(shift, cashierRole, amount);
            }
        }
    }

    public void setStoreKeeperWeekReqs(Branch branch, int amount) {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Role storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        for (Shift shift : shiftsReqs.keySet()) {
            Branch shiftBranch = shift.getBranch();
            LocalDate shiftDate = shift.getDate();
            if (shiftBranch == branch && !shiftDate.isBefore(startOfWeek) && !shiftDate.isAfter(endOfWeek)) {
                set(shift, storekeeperRole, amount);
            }
        }
    }
}