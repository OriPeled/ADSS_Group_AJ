package dev.Workers.Service;

import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Objects.DriverRole;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.RoleRegistry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrates role assignments and queries.
 * State is now maintained strictly within the Employee objects.
 */
public class RoleService {
    private final EmployeeManager employeeManager = EmployeeManager.getInstance();
    private final RoleRegistry roleRegistry = RoleRegistry.getInstance();

    private static RoleService instance;

    private RoleService() {}

    public static RoleService getInstance() {
        if (instance == null) {
            instance = new RoleService();
        }
        return instance;
    }

    public void addRoleToEmployee(int id, Role newRole) {
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);

        if (newRole.isQualified(id)) {
            throw new IllegalArgumentException("Employee already qualified for this role.");
        }

        emp.addRole(newRole);
    }

    public void removeAll(int id) {
        Employee emp = employeeManager.getById(id);
        emp.removeAllRoles();
    }

    public void removeSingleItem(int id, Role role) {
        Employee emp = employeeManager.getById(id);

        if (!role.isQualified(id)) {
            throw new IllegalArgumentException("Employee isn't qualified for this role.");
        }

        emp.removeRole(role);
    }

    public List<Role> getListById(int id) {
        Employee emp = employeeManager.getById(id);
        // Assuming your Employee class now has a getRoles() method returning a List or Set
        return new ArrayList<>(emp.getRoles());
    }

    public String getFormattedListById(int id) {
        // 1. Get the actual roles straight from the employee!
        List<Role> employeeRoles = getListById(id);

        if (employeeRoles.isEmpty()) {
            return "No roles found for this ID.";
        }

        List<String> displayLines = new ArrayList<>();
        List<String> driverLicenses = new ArrayList<>();

        // 2. Sort the roles: Standard roles go to the UI, Driver licenses get grouped
        for (Role role : employeeRoles) {
            if (role instanceof DriverRole driverRole) {
                // Collect just the license letters (A, B, C)
                driverLicenses.add(driverRole.getRequiredLicense().name());
            } else {
                // Add standard roles (Cashier, Storekeeper) as normal
                displayLines.add(role.getName());
            }
        }

        // 3. If they have driver licenses, merge them into one beautiful line
        if (!driverLicenses.isEmpty()) {
            displayLines.add("Driver (" + String.join(", ", driverLicenses) + ")");
        }

        // 4. Print with sequential numbering (1. Cashier \n 2. Driver (A, B))
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < displayLines.size(); i++) {
            sb.append(i + 1).append(". ").append(displayLines.get(i)).append("\n");
        }

        return sb.toString().trim();
    }

    public List<Role> availableToAddRoles(int id) {
        Employee emp = employeeManager.getById(id);
        return roleRegistry.getAllRoles().stream()
                .filter(role -> !role.isQualified(id))
                .collect(Collectors.toList());
    }

    public String getFormattedAvailableRoles(int id) {
        List<Role> allRoles = roleRegistry.getAllRoles();
        StringBuilder sb = new StringBuilder();
        int displayIndex = 1; // Create a separate counter

        for (Role role : allRoles) {
            if (!role.isQualified(id)) {
                sb.append(displayIndex).append(". ").append(role.getName()).append("\n");
                displayIndex++; // Only increment when we find an available role
            }
        }

        return sb.length() > 0 ? sb.toString().trim() : "No available roles to add.";
    }

    /**
     * Gets all employees qualified for a specific role.
     * Replaces the old map iteration with a clean stream filter.
     */
    public List<Integer> getListByRole(Role role) {
        // Assuming EmployeeManager has a method like getAllEmployees() that returns a Collection of Employee objects
        return employeeManager.getEmployeesList().stream()
                .filter(emp -> role.isQualified(emp.getId()))
                .map(Employee::getId)
                .collect(Collectors.toList());
    }

    public boolean isQualified(int id, Role role) {
        return role.isQualified(id);
    }
}
