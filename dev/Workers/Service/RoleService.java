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
import java.util.Set;
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

        if (newRole instanceof DriverRole) {
            LicenseType empLicenseType = emp.getLicenseType();
            newRole = new DriverRole(empLicenseType);
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

    public List<Role> availableToAddRoles(int id) {
        Employee emp = employeeManager.getById(id);
        return roleRegistry.getAllRoles().stream()
                .filter(role -> !role.isQualified(id))
                .collect(Collectors.toList());
    }

    private String getBaseRoleName(Role role) {
        if (role instanceof DriverRole) {
            return "Driver";
        }
        return role.getName();
    }

    public String getFormattedListById(int id) {
        // 1. Get the roles the employee actually has
        List<Role> employeeRoles = getListById(id);

        // 2. Map them to base names (e.g., "Driver (A)" -> "Driver") and remove duplicates
        List<String> displayRoles = employeeRoles.stream()
                .map(this::getBaseRoleName)
                .distinct()
                .collect(Collectors.toList());

        if (displayRoles.isEmpty()) {
            return "No roles found for this ID.";
        }

        StringBuilder sb = new StringBuilder();
        // 3. Print sequentially (1, 2, 3...)
        for (int i = 0; i < displayRoles.size(); i++) {
            sb.append(i + 1).append(". ").append(displayRoles.get(i)).append("\n");
        }

        return sb.toString().trim();
    }

    public String getFormattedAvailableRoles(int id) {
        // 1. Figure out which base roles the employee already has
        List<Role> employeeRoles = getListById(id);
        Set<String> currentBaseRoles = employeeRoles.stream()
                .map(this::getBaseRoleName)
                .collect(Collectors.toSet());

        // 2. Filter the registry: Only keep base roles they don't already have!
        List<String> availableBaseRoles = roleRegistry.getAllRoles().stream()
                .map(this::getBaseRoleName)
                .distinct() // Prevents "Driver" from showing up 4 times
                .filter(baseName -> !currentBaseRoles.contains(baseName))
                .collect(Collectors.toList());

        if (availableBaseRoles.isEmpty()) {
            return "No available roles to add.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < availableBaseRoles.size(); i++) {
            sb.append(i + 1).append(". ").append(availableBaseRoles.get(i)).append("\n");
        }

        return sb.toString().trim();
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