package dev.Workers.Service;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.RoleManager;

import java.util.*;

/**
 * Manages roles assigned to employees.
 * <p>
 * This class is implemented as a Singleton.
 * It supports assigning roles to employees, removing roles,
 * and querying employees by roles.
 */
public class RoleService {
    // maps employee ID to list of roles
    private final RoleManager roleManager;
    private static RoleService instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private RoleService() {
        this.roleManager = RoleManager.getInstance();
    }

    /**
     * @return the single instance of RoleManager
     */
    public static RoleService getInstance() {
        if (instance == null) {
            instance = new RoleService();
        }
        return instance;
    }

    /**
     * Adds a single role to an employee
     *
     * @param id   employee ID
     * @param role role to add
     */
    public void addRoleToEmployee(int id, Role role) {
        roleManager.addRoleToEmployee(id, role);
    }

    /**
     * Returns all employees that have the "Manager" role
     *
     * @return list of employee IDs who are managers
     */
    public List<Integer> getAllManagers() {
        return roleManager.getAllManagers();
    }

    /**
     * Removes all roles from an employee
     *
     * @param id employee ID
     */
    public void removeAllRoles(int id) {
        roleManager.removeAll(id);
    }

    /**
     * Removes a specific role from an employee
     *
     * @param id   employee ID
     * @param role role to remove
     */
    public void removeSpecificRole(int id, Role role) {
        roleManager.removeSingleItem(id, role);
    }

    /**
     * Retrieves all roles assigned to an employee
     *
     * @param id employee ID
     * @return list of roles (empty list if none exist)
     */
    public List<Role> getEmployeeRoles(int id) {
        // Domain returns an empty list if not found, which is safe
        return roleManager.getListById(id);
    }

    /**
     * Returns all employees who have a specific role
     *
     * @param role role to search for
     * @return list of employee IDs
     */
    public List<Integer> getEmployeesByRole(Role role) {
        return roleManager.getListByRole(role);
    }

    /**
     *
     * @param id
     * @param role
     * @return true if role beloge to id , otherwise false
     */
    public boolean hasRole(int id, Role role) {
        return roleManager.containsRole(id, role);
    }

    public void promoteDemote(int id) {
        List<Role> roles = roleManager.getListById(id);

        if (roles == null) {
            throw new IllegalArgumentException("Employee roles not found for id: " + id);
        }

        if (roles.isEmpty()) {
            throw new IllegalStateException("Employee has no roles. Cannot promote/demote.");
        }

        roleManager.promoteDemote(id);
    }

}
