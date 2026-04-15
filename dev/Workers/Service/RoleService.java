package dev.Workers.Service;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.IListManager;

import java.util.*;

import static dev.Workers.domain.Enums.Role.shiftManager;

/**
 * Manages roles assigned to employees.
 * <p>
 * This class is implemented as a Singleton.
 * It supports assigning roles to employees, removing roles,
 * and querying employees by roles.
 */
public class RoleService implements IListManager<Role> {
    // maps employee ID to list of roles
    private Map<Integer, List<Role>> employeeRoles;


    private static RoleService instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private RoleService() {
        this.employeeRoles = new HashMap<>();
    }

    /**
     * @return the single instance of RoleManager
     */
    public static RoleService getInstance() {
        if (instance == null)
            instance = new RoleService();
        return instance;
    }

    /**
     * Adds a full list of roles to an employee (replaces existing roles)
     *
     * @param id    employee ID
     * @param items list of roles
     */
    @Override
    public void addFullList(int id, List<Role> items) {
        employeeRoles.put(id, items);
    }

    /**
     * Adds a single role to an employee
     *
     * @param id   employee ID
     * @param role role to add
     */
    @Override
    public void addSingleItem(int id, Role role) {
        if (!employeeRoles.containsKey(id)) {
            employeeRoles.put(id, new ArrayList<>());
        }
        if (!containsRole(id, role)) {
            employeeRoles.get(id).add(role);
        }
    }

    /**
     * Returns all employees that have the "Manager" role
     *
     * @return list of employee IDs who are managers
     */
    public List<Integer> getAllManagers() {
        List<Integer> managers = new ArrayList<>();

        for (Integer id : employeeRoles.keySet()) {
            List<Role> roles = getListById(id);
            for (Role role : roles) {
                if (role == shiftManager) {
                    managers.add(id);
                }
            }
        }
        return managers;
    }

    /**
     * Removes all roles from an employee
     *
     * @param id employee ID
     */
    @Override
    public void removeAll(int id) {
        employeeRoles.remove(id);
    }

    /**
     * Removes a specific role from an employee
     *
     * @param id   employee ID
     * @param role role to remove
     */
    @Override
    public void removeSingleItem(int id, Role role) {
        if (employeeRoles.containsKey(id)) {
            employeeRoles.get(id).remove(role);
            if (employeeRoles.get(id).isEmpty()) {
                employeeRoles.remove(id);
            }
        }
    }

    /**
     * Retrieves all roles assigned to an employee
     *
     * @param id employee ID
     * @return list of roles (empty list if none exist)
     */
    @Override
    public List<Role> getListById(int id) {
        return employeeRoles.getOrDefault(id, new ArrayList<>());
    }


    /**
     * Returns all employees who have a specific role
     *
     * @param role role to search for
     * @return list of employee IDs
     */
    public List<Integer> getListByRole(Role role) {
        List<Integer> qualifiedEmployees = new ArrayList<>();
        for (Integer id : employeeRoles.keySet()) {
            List<Role> roles = getListById(id);
            if (roles.contains(role)) {
                qualifiedEmployees.add(id);
            }
        }
        return qualifiedEmployees;
    }

    /**
     *
     * @param Id
     * @param role
     * @return true if role beloge to id , otherwise false
     */
    public boolean containsRole(int Id,Role role) {
        for (Integer id : employeeRoles.keySet()) {
            List<Role> roles = getListById(id);
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

}
