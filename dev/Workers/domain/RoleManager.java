package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static dev.Workers.domain.Enums.Role.shiftManager;

/**
 * Manages roles assigned to employees.
 * <p>
 * This class is implemented as a Singleton.
 * It supports assigning roles to employees, removing roles,
 * and querying employees by roles.
 */
public class RoleManager  {    // maps employee ID to list of roles
    private Map<Integer, List<Role>> employeeRoles;
    private EmployeeManager employeeManager=EmployeeManager.getInstance() ;

    private static RoleManager instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private RoleManager() {
        this.employeeRoles = new HashMap<>();
    }

    /**
     * @return the single instance of RoleManager
     */
    public static RoleManager getInstance() {
        if (instance == null)
            instance = new RoleManager();
        return instance;
    }

    /**
     * Adds a single role to an employee
     *
     * @param id   employee ID
     * @param newRole role to add
     */
    public void addRoleToEmployee(int id, Role newRole) {
        employeeManager.validateEmployeeBasic(id);
        if (!employeeRoles.containsKey(id)) {
            employeeRoles.put(id, new ArrayList<>());
        }
        if (!hasRole(id, newRole)) {
            employeeRoles.get(id).add(newRole);
        } else {
            throw new IllegalArgumentException("Employee already qualified for this role.");
        }
        /*if (newRole == shiftManager){
            for (Role role : Role.values()) {
                if (!hasRole(id, role))
                    addRoleToEmployee(id, role);
            }
        }*/
    }

    /**
     * Returns all employees that have the "Manager" role
     *
     * @return list of employee IDs who are managers
     */
    /*public List<Integer> getAllManagers() {
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
    }*/

    /**
     * Removes all roles from an employee
     *
     * @param id employee ID
     */

    public void removeAll(int id) {
        employeeRoles.remove(id);
    }

    /**
     * Removes a specific role from an employee
     *
     * @param id   employee ID
     * @param role role to remove
     */

    public void removeSingleItem(int id, Role role) {
        if (getFormattedListById(id).isEmpty())
            throw new IllegalArgumentException("Employee isn't qualified for any role.");
        if (!hasRole(id, role))
            throw new IllegalArgumentException("Employee isn't qualified for this role.");
        if (employeeRoles.containsKey(id)) {
            employeeRoles.get(id).remove(role);
            if (employeeRoles.get(id).isEmpty()) {
                employeeRoles.remove(id);
            }
        }
    }

    public List<Role> getListById(int id) {
        return employeeRoles.getOrDefault(id, new ArrayList<>());
    }

    /**
     * Retrieves all roles assigned to an employee
     *
     * @param id employee ID
     * @return list of roles (empty list if none exist)
     */

    public String getFormattedListById(int id) {
        // 1. Retrieve the list using your existing logic
        List<Role> roles = employeeRoles.getOrDefault(id, new ArrayList<>());

        // 2. Handle the empty case
        if (roles.isEmpty()) {
            return "No roles found for this ID.";
        }

        // 3. Build the numbered string
        StringBuilder sb = new StringBuilder();
        Role[] allRoles = Role.values();
        for (int i = 0; i < allRoles.length; i++) {
            Role role = allRoles[i];
            if (hasRole(id, role)) {
                sb.append(i + 1).append(". ").append(role).append("\n");
            }
        }
        /*for (int i = 0; i < roles.size(); i++) {
            sb.append(i + 1).append(". ").append(roles.get(i)).append("\n");
        }*/

        return sb.toString().trim();
    }

    public List<Role> availableToAddRoles(int id) {
        List<Role> rolesList = new ArrayList<>();
        for (Role role : Role.values()) {
            if (!hasRole(id, role)) {
                rolesList.add(role);
            }
        }
        return rolesList;
    }

    public String getFormattedAvailableRoles(int id) {
        List<Role> roles = availableToAddRoles(id);

        if (roles.isEmpty()) {
            return "No available roles to add.";
        }

        StringBuilder sb = new StringBuilder();
        /*for (int i = 0; i < roles.size(); i++) {
            sb.append(i + 1).append(". ").append(roles.get(i)).append("\n");
        }*/
        Role[] allRoles = Role.values();
        for (int i = 0; i < allRoles.length; i++) {
            Role role = allRoles[i];
            if (!hasRole(id, role)) {
                sb.append(i + 1).append(". ").append(role).append("\n");
            }
        }

        return sb.toString().trim();
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
     * @param id
     * @param role
     * @return true if role beloge to id , otherwise false
     */
    public boolean hasRole(int id, Role role) {
        List<Role> roles = getListById(id);
        return roles != null && roles.contains(role);
    }
}
