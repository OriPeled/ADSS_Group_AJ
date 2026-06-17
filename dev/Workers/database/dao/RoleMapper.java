package dev.Workers.database.dao;

import dev.Workers.domain.RoleRegistry;
import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Objects.DriverRole;
import dev.Workers.domain.Objects.Role;

/**
 * Helper that maps a polymorphic {@link Role} to/from its stored columns
 * (role_kind, role_name, license), shared by every DAO that persists roles
 * (employee_roles, shift_requirements, shift_assignments).
 * <p>
 * Restored roles are resolved against the {@link RoleRegistry} so they keep
 * identity with the in-memory registry instances.
 */
final class RoleMapper {
    private static final RoleRegistry roleRegistry = RoleRegistry.getInstance();

    private RoleMapper() {
    }

    static String kindOf(Role role) {
        return (role instanceof DriverRole) ? "DRIVER" : "STANDARD";
    }

    /** Standard roles store their name; driver roles store null here. */
    static String roleNameOf(Role role) {
        return (role instanceof DriverRole) ? null : role.getName();
    }

    /** Driver roles store their license name; standard roles store null here. */
    static String licenseOf(Role role) {
        return (role instanceof DriverRole driverRole)
                ? driverRole.getRequiredLicense().name()
                : null;
    }

    /**
     * Rebuilds the canonical Role instance from stored columns.
     *
     * @throws IllegalStateException if the stored role is not defined in the registry
     */
    static Role resolve(String kind, String roleName, String license) {
        try {
            if ("DRIVER".equals(kind)) {
                LicenseType licenseType = LicenseType.valueOf(license);
                return roleRegistry.getRoleByName("Driver (" + licenseType + ")");
            }
            return roleRegistry.getRoleByName(roleName);
        } catch (IllegalArgumentException e) {
            String stored = "DRIVER".equals(kind) ? ("Driver (" + license + ")") : roleName;
            throw new IllegalStateException(
                    "Stored role '" + stored + "' is not defined in the RoleRegistry; "
                            + "cannot restore it from the database.", e);
        }
    }
}