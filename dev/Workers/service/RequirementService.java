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
        requirementHandler.getDriverReqs(branch);
    }

    public void getStoreKeeperReqs(Branch branch) {
        requirementHandler.getStoreKeeperReqs(branch);
    }

    public void setCashierWeekReqs(Branch branch, int amount) {
        requirementHandler.setCashierWeekReqs(branch, amount);
    }

    public void setStoreKeeperWeekReqs(Branch branch, int amount) {
        requirementHandler.setStoreKeeperWeekReqs(branch, amount);
    }
}
