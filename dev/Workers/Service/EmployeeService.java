package dev.Workers.Service;


import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.EmployeeTerms;

import java.time.LocalDate;

/**
 * Service class for Employee business logic.
 * Handles validations, cross-service communication, and delegates data storage to EmployeeManager.
 */
public class EmployeeService {
    private static EmployeeService instance;
    private static final EmployeeManager employeeManager = EmployeeManager.getInstance();
    private static final AccessService accessService = AccessService.getInstance();
    /**
     * Private constructor to enforce the Singleton pattern.
     */
    private EmployeeService() {
    }

    /**
     * Retrieves the single instance of the EmployeeService.
     *
     * @return the singleton instance of EmployeeService
     */
    public static EmployeeService getInstance() {
        if (instance == null) {
            instance = new EmployeeService();
        }
        return instance;
    }

    /**
     * Creates and adds a new employee to the system with business validations.
     *
     * @param name        the name of the employee
     * @param id          the unique ID of the employee
     * @param bankAccount the bank account number
     * @param salary      the starting salary
     * @param terms       the employment terms
     * @param startDate   the start date of employment
     * @throws IllegalArgumentException if the ID already exists, or if salary or bank account are invalid
     */
    public void add(String name, int id, int bankAccount, double salary, EmployeeTerms terms, LocalDate startDate) {
        // 1. Business logic validations
        if (employeeManager.isEmployee(id)) {
            throw new IllegalArgumentException("Cannot add employee: Employee ID " + id + " already exists.");
        }
        if (salary <= 0) {
            throw new IllegalArgumentException("Cannot add employee: Salary must be a positive number.");
        }
        if (bankAccount <= 0) {
            throw new IllegalArgumentException("Cannot add employee: Invalid bank account details.");
        }

        Employee newEmp = new Employee(name, id, bankAccount, salary, terms, startDate);
        employeeManager.add(id, newEmp);
    }

    /**
     * Terminates an employee and removes their system access.
     *
     * @param id the unique ID of the employee to remove
     * @throws IllegalArgumentException if the employee is not found or is already inactive

    public void remove(int id) {
        Employee emp = employeeManager.getById(id);

        if (emp.isActive()) {
            // Update termination date (business logic)
            emp.terminateEmployee(LocalDate.now());

            // Remove system access
            try {
                accessService.removeUser(id);
            } catch (IllegalArgumentException e) {
                // Ignore if the employee didn't have a configured password
            }
        } else {
            throw new IllegalArgumentException("Cannot remove: Employee ID " + id + " already inactive.");
        }
    } */

    public void remove(int id) {
        employeeManager.remove(id); // terminate (throws if invalid)
        if (accessService.isRegisteredUser(id)) {
            accessService.removeUser(id); // clean up access
        }
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param id the unique ID of the employee
     * @return the Employee object, or null if not found
     */
    public Employee getEmployee(int id) {
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new NullPointerException("Employee doesn't exist");
        }
        return emp;
    }
    public boolean isEmployee(int id) {
        return employeeManager.isEmployee(id);
    }

}