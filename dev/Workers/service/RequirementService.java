package dev.Workers.service;

import dev.Workers.domain.Objects.*;
import dev.Workers.domain.RequirementHandler;

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

    // TP holds LicenseType enum
    public void getDriverReqs(Branch branch) {
        requirementHandler.getDriverWeeklyReqs(branch);
    }

    public void getStoreKeeperReqs(Branch branch) {
        requirementHandler.getStoreKeeperWeeklyReqs(branch);
    }

    public void initWeeklyReqs(String rolename, Branch branch, int amount) {
        requirementHandler.initWeeklyReqs(rolename, branch, amount);
    }
}
