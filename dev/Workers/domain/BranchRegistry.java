package dev.Workers.domain;

import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Enums.LicenseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces the branch.values() enum functionality.
 * Maintains the master list of all available branchs in the company.
 */
public class BranchRegistry {
    private static BranchRegistry instance;
    private final List<Branch> allBranches;

    private BranchRegistry() {
        this.allBranches = new ArrayList<>();

        allBranches.add(new Branch("Beer-Sheva"));
        allBranches.add(new Branch("Dimona"));
        allBranches.add(new Branch("Ofakim"));
        allBranches.add(new Branch("Rahat"));
    }

    public static BranchRegistry getInstance() {
        if (instance == null) {
            instance = new BranchRegistry();
        }
        return instance;
    }

    public List<Branch> getAllBranches() {
        return allBranches;
    }

    public int getBranchesAmount() {
        return allBranches.size();
    }

    public Branch getBranchByName(String name) {
        return allBranches.stream()
                .filter(r -> r.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + name));
    }
}
