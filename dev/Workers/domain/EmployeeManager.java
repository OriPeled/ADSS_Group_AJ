package dev.Workers.domain;

import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.EmployeeTerms;
import dev.Workers.domain.Objects.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.Workers.domain.Enums.UserResponse.demoted;
import static dev.Workers.domain.Enums.UserResponse.promoted;

/**
 * Manages all employees in the system.
 * This class is implemented as a Singleton.
 * Provides functionality to add, remove, and retrieve employees.
 */
public class EmployeeManager   {
    private Map<Integer, Employee> employees;   // Employee ID to Employee
    private static EmployeeManager instance;

    /**
     * Private constructor to enforce Singleton pattern
     */
    private EmployeeManager() {
        this.employees = new HashMap<>();
    }

    /**
     * @return the single instance of EmployeeManager
     */
    public static EmployeeManager getInstance() {
        if (instance == null) {
            instance = new EmployeeManager();
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
        Employee employee = getById(id);
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
    }

    // fires an employee, leaves him in the system
    public void fire(int id) {
        Employee emp = getById(id);
        if (!emp.isActive(LocalDate.now())) {
            throw new IllegalArgumentException("Employee " + id + " not found or already inactive.");
        }
        emp.terminateEmployee(LocalDate.now());
    }

    public void rehire(int id) {
        if (!isEmployee(id)) throw new IllegalArgumentException("Unknown ID: " + id);
        if (getById(id).isActive(LocalDate.now())) throw new IllegalArgumentException("Employee " + id + " already active.");
        if(getById(id).toBeActive()) {throw new IllegalArgumentException("Employee " + id + " cannot be reactivated yet. Termination date: " + getById(id).getEndLocalDate());}
        employees.get(id).activateEmployee();
    }

    public Employee getById(int id) {
        return employees.get(id);
    }

    public void validateEmployeeBasic(int id, LocalDate date) {
        if (!isEmployee(id)) throw new IllegalArgumentException("Unknown ID: " + id);
        if (!getById(id).isActive(date)) throw new IllegalArgumentException("Employee " + id + " is inactive at " + date);
    }

    public UserResponse promoteDemote(int id) {
        Employee employee = getById(id);

        employee.setManager(!employee.isManager());

        return employee.isManager() ? promoted : demoted;
    }

    public List<Integer> getListByRole(Role role) {
        // Assuming EmployeeManager has a method like getAllEmployees() that returns a Collection of Employee objects
        return getEmployeesList().stream()
                .filter(emp -> role.isQualified(emp.getId()))
                .map(Employee::getId)
                .collect(Collectors.toList());
    }
}