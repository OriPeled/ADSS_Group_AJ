package dev.Workers.Service;

import dev.Workers.domain.Objects.*;
import dev.Workers.domain.RequirementsHandler;

public class RequirementsService {
    private static RequirementsHandler requirementsHandler;

    private static RequirementsService instance;

    public static RequirementsService getInstance() {
        if (instance == null) {
            instance = new RequirementsService();
        }
        return instance;
    }

    private RequirementsService() {
        requirementsHandler = RequirementsHandler.getInstance();
    }

    // TP holds LicenseType enum
    public void getDriverReqs(Branch branch) {
        requirementsHandler.getDriverReqs(branch);
    }

    public void getStoreKeeperReqs(Branch branch) {
        requirementsHandler.getStoreKeeperReqs(branch);
    }

    public void setCashierWeekReqs(Branch branch, int amount) {
        requirementsHandler.setCashierWeekReqs(branch, amount);
    }

    public void setStoreKeeperWeekReqs(Branch branch, int amount) {
        requirementsHandler.setStoreKeeperWeekReqs(branch, amount);
    }
}
