package dev.Workers.service;

import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Objects.*;
import dev.Workers.domain.RequirementHandler;
import dev.Workers.domain.RoleRegistry;
import dev.Workers.setup.TransportService;
import dev.Workers.utils.Parser;

import java.util.List;
import java.util.Map;

public class RequirementService {
    private RequirementHandler requirementHandler;
    private ShiftService shiftService;
    private RoleRegistry roleRegistry;

    private static RequirementService instance;

    public static RequirementService getInstance() {
        if (instance == null) {
            instance = new RequirementService();
        }
        return instance;
    }

    private RequirementService() {
        requirementHandler = RequirementHandler.getInstance();
        shiftService = ShiftService.getInstance();
        roleRegistry = RoleRegistry.getInstance();
    }

    public void getDriverReqs(Branch branch) {
        String branchName = branch.getName();
        for (Shift shift : requirementHandler.getShiftsForWeek(branch)) {
            String rawReqs = TransportService.getDriverRequirements(
                    branchName, shift.getDate(), shift.getStartTime(), shift.getEndTime()
            );

            Map<LicenseType, Integer> driverReqs = Parser.stringToDriverReqs(rawReqs);

            if (driverReqs != null && !driverReqs.isEmpty()) {
                for (Map.Entry<LicenseType, Integer> entry : driverReqs.entrySet()) {
                    Role driverRole = roleRegistry.getRoleByName("Driver (" + entry.getKey().name() + ")");

                    if (driverRole != null) {
                        shiftService.initTransportRequirement(shift, driverRole, entry.getValue());
                    }
                }
            }
        }
    }

    public void getStoreKeeperReqs(Branch branch) {
        String branchName = branch.getName();
        Role storekeeperRole = roleRegistry.getRoleByName("Storekeeper");
        for (Shift shift : requirementHandler.getShiftsForWeek(branch)) {
            int amount = TransportService.getStorekeeperRequirements(
                    branchName, shift.getDate(), shift.getStartTime(), shift.getEndTime()
            );

            shiftService.initTransportRequirement(shift, storekeeperRole, amount);
        }
    }

    public void initWeeklyReqs(String rolename, Branch branch, int amount) {
        Role roleToSet = roleRegistry.getRoleByName(rolename);
        List<Shift> weekShifts = requirementHandler.getShiftsForWeek(branch);

        for (Shift shift : weekShifts) {
            shiftService.setRequirement(shift, roleToSet, amount);
        }
    }
}