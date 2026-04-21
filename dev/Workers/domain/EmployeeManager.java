package dev.Workers.domain;

import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.EmployeeTerms;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
    public void add(String name, int id, int bankAccount, double salary, EmployeeTerms terms, LocalDate startDate) {
        if (isEmployee(id)) {
            throw new IllegalArgumentException("Employee ID " + id + " already exists.");
        }
        Employee newEmp = new Employee(name, id, bankAccount, salary, terms, startDate);
        employees.put(newEmp.getId(), newEmp);
    }

    // fires an employee, leaves him in the system
    public void fire(int id) {
        Employee emp = employees.get(id);
        if (emp == null || !emp.isActive()) {
            throw new IllegalArgumentException("Employee " + id + " not found or already inactive.");
        }
        emp.terminateEmployee(LocalDate.now());
    }

    public void rehire(int id) {
        if (!isEmployee(id)) throw new IllegalArgumentException("Unknown ID: " + id);
        if (getById(id).isActive()) throw new IllegalArgumentException("Employee " + id + " already active.");
        employees.get(id).activateEmployee();
    }

    public Employee getById(int id) {
        return employees.get(id);
    }

    public void validateEmployeeBasic(int id) {
        if (!isEmployee(id)) throw new IllegalArgumentException("Unknown ID: " + id);
        if (!getById(id).isActive()) throw new IllegalArgumentException("Employee " + id + " is inactive.");
    }

    public void promoteDemote(int id) {
        Employee employee = getById(id);
        employee.setManager(!employee.isManager());
    }
}