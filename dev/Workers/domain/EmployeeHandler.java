package dev.Workers.domain;

import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Objects.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.Workers.domain.Enums.UserResponse.*;
import dev.Workers.database.dao.EmployeeDaoSQL;

/**
 * Manages all employees in the system.
 * This class is implemented as a Singleton.
 * Provides functionality to add, remove, and retrieve employees.
 */
public class EmployeeHandler {
    private Map<Integer, Employee> employees;   // Employee ID to Employee
    private static EmployeeHandler instance;
    private final RoleRegistry roleRegistry = RoleRegistry.getInstance();
    private final EmployeeDaoSQL employeeDao = EmployeeDaoSQL.getInstance();

    private EmployeeHandler() {
        this.employees = new HashMap<>();
    }

    public static EmployeeHandler getInstance() {
        if (instance == null) {
            instance = new EmployeeHandler();
        }
        return instance;
    }

    public Map<Integer, Employee> getEmployees() {
        return employees;
    }

    public List<Employee> getEmployeesList() {
        return new ArrayList<>(employees.values());
    }

    /**
     * Checks if employee exists in the system
     *
     * @param id employee ID
     * @return true if exists, false otherwise
     */
    public boolean isEmployee(int id) {
        Employee employee = getEmployee(id);
        return employee != null;
    }

    /**
     * Creates and adds a new employee to the system
     *
     * @param name        employee name
     * @param id          employee ID
     * @param bankAccount bank account number
     * @param salary      employee salary
     * @param terms       employment terms
     * @param startDate   employment start date
     */
    public void add(String name, int id, Branch branch, int bankAccount, double salary, EmployeeTerms terms,
                    LocalDate startDate) {
        if (isEmployee(id)) {
            throw new IllegalArgumentException("Employee ID " + id + " already exists.");
        }
        Employee newEmp = new Employee(name, id, branch, bankAccount, salary, terms, startDate);
        employees.put(newEmp.getId(), newEmp);
        employeeDao.save(newEmp);
    }

    public UserResponse fireRehire(int id) {
        Employee emp = getEmployee(id);

        if (emp.isTerminated()) {
            emp.activateEmployee();
            employeeDao.update(emp);
            return rehired;
        } else {
            emp.terminateEmployee(LocalDate.now());
            employeeDao.update(emp);
            return fired;
        }
    }

    public UserResponse fireRehire(int id, LocalDate date) {
        Employee emp = getEmployee(id);

        if (emp.isTerminated()) {
            emp.activateEmployee();
            employeeDao.update(emp);
            return rehired;
        } else {
            emp.terminateEmployee(date);
            employeeDao.update(emp);
            return fired;
        }
    }

    // fires an employee, leaves him in the system
    public void fire(int id) {
        Employee emp = getEmployee(id);
        if (emp.isTerminated()) {
            throw new IllegalArgumentException("Employee " + id + " already terminated.");
        }
        emp.terminateEmployee(LocalDate.now());
        employeeDao.update(emp);
    }

    public void rehire(int id) {
        Employee emp = getEmployee(id);

        if (!emp.isTerminated()) {
            throw new IllegalArgumentException("Employee " + id + " is already fully active (not terminated).");
        }

        if (emp.toBeActive()) {
            throw new IllegalArgumentException("Employee " + id + " cannot be reactivated yet. Termination date: " + emp.getEndLocalDate());
        }

        emp.activateEmployee();
        employeeDao.update(emp);
    }

    public Employee getEmployee(int id) {
        return employees.get(id);
    }

    public void validateEmployeeBasic(int id, LocalDate date) {
        if (!isEmployee(id)) throw new IllegalArgumentException("Unknown ID: " + id);
        if (!getEmployee(id).isActive(date)) throw new IllegalArgumentException("Employee " + id + " is inactive as for " + date);
    }

    public UserResponse promoteDemote(int id) {
        Employee employee = getEmployee(id);

        employee.setManager(!employee.isManager());
        employeeDao.update(employee);

        return employee.isManager() ? promoted : demoted;
    }

    public List<Integer> getListByRole(Role role) {
        return getEmployeesList().stream()
                .filter(emp -> role.isQualified(emp.getId()))
                .map(Employee::getId)
                .collect(Collectors.toList());
    }

    public void addRole(int id, Role newRole) {
        validateEmployeeBasic(id, LocalDate.now());
        Employee emp = getEmployee(id);

        if (newRole.isQualified(id)) {
            throw new IllegalArgumentException("Employee already qualified for this role.");
        }

        emp.addRole(newRole);
        employeeDao.update(emp);
    }

    public void removeRole(int id, Role role) {
        Employee emp = getEmployee(id);

        if (!role.isQualified(id)) {
            throw new IllegalArgumentException("Employee isn't qualified for this role.");
        }

        emp.removeRole(role);
        employeeDao.update(emp);
    }

    public void removeAll(int id) {
        Employee emp = getEmployee(id);
        emp.removeAllRoles();
        employeeDao.update(emp);
    }

    public List<Role> getRoles(int id) {
        Employee emp = getEmployee(id);
        // Assuming your Employee class now has a getRoles() method returning a List or Set
        return new ArrayList<>(emp.getRoles());
    }

    public List<Role> availableToAddRoles(int id) {
        Employee emp = getEmployee(id);
        return roleRegistry.getAllRoles().stream()
                .filter(role -> !role.isQualified(id))
                .collect(Collectors.toList());
    }

    public String getFormattedRolesList(int id) {
        // 1. Get the actual roles straight from the employee!
        List<Role> employeeRoles = getRoles(id);

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

}