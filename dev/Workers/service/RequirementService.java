package dev.Workers.service;

import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Objects.*;
import dev.Workers.domain.RequirementHandler;
import dev.Workers.setup.TransportService;
import dev.Workers.utils.Parser;

import java.util.Map;

public class RequirementService {
    private static RequirementHandler requirementHandler;

    private static RequirementService instance;

    public static RequirementService getInstance() {
        if (instance == null) {
            instance = new RequirementService();
        }
        return instance;
    }

    private RequirementService() {
        requirementHandler = RequirementHandler.getInstance();
    }

    public void getDriverReqs(Branch branch) {
        String branchName = branch.getName();

        for (Shift shift : requirementHandler.getShiftsForWeek(branch)) {

            String rawReqs = TransportService.getDriverRequirements(
                    branchName, shift.getDate(), shift.getStartTime(), shift.getEndTime()
            );

            Map<LicenseType, Integer> driverReqs = Parser.stringToDriverReqs(rawReqs);

            requirementHandler.applyDriverReqsToShift(shift, driverReqs);
        }
    }

    public void getStoreKeeperReqs(Branch branch) {
        String branchName = branch.getName();

        for (Shift shift : requirementHandler.getShiftsForWeek(branch)) {

            int amount = TransportService.getStorekeeperRequirements(
                    branchName, shift.getDate(), shift.getStartTime(), shift.getEndTime()
            );

            requirementHandler.applyStorekeeperReqToShift(shift, amount);
        }
    }

    public void initWeeklyReqs(String rolename, Branch branch, int amount) {
        requirementHandler.initWeeklyReqs(rolename, branch, amount);
    }
}
