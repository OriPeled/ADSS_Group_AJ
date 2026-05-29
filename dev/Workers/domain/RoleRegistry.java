package dev.Workers.domain;

import dev.Workers.domain.Objects.DriverRole;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.StandardRole;
import dev.Workers.domain.Enums.LicenseType;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces the Role.values() enum functionality.
 * Maintains the master list of all available roles in the company.
 */
public class RoleRegistry {
    private static RoleRegistry instance;
    private final List<Role> allRoles;

    private RoleRegistry() {
        this.allRoles = new ArrayList<>();

        allRoles.add(new StandardRole("Cashier"));
        allRoles.add(new StandardRole("Storekeeper"));

        allRoles.add(new DriverRole(LicenseType.A));
        allRoles.add(new DriverRole(LicenseType.B));
        allRoles.add(new DriverRole(LicenseType.C));
        allRoles.add(new DriverRole(LicenseType.D));
    }

    public static RoleRegistry getInstance() {
        if (instance == null) {
            instance = new RoleRegistry();
        }
        return instance;
    }

    public List<Role> getAllRoles() {
        return allRoles;
    }

    public int getRolesAmount() {
        return allRoles.size();
    }

    public Role getRoleByName(String name) {
        return allRoles.stream()
                .filter(r -> r.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name));
    }
}
