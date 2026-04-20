package dev.Workers.Service;


import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.Objects.EmployeeTerms;

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
        if (id <= 0) {
            throw new IllegalArgumentException("Cannot add employee: ID must be a positive number.");
        }
        if (bankAccount <= 0) {
            throw new IllegalArgumentException("Cannot add employee: Invalid bank account details.");
        }
        if (salary <= 0) {
            throw new IllegalArgumentException("Cannot add employee: Salary must be a positive number.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Cannot add employee: Start date cannot be before current date.");
        }
        if (terms.getJobStatus() == null) {
            throw new IllegalArgumentException("Cannot add employee: Job status cannot be null.");

        }
        if (terms.getSalaryType() == null) {
            throw new IllegalArgumentException("Cannot add employee: Salary type cannot be null.");
        }
        if(terms.getRestDays() < 1 || terms.getRestDays() > 7) {
            throw new IllegalArgumentException("Cannot add employee: Rest days must be between 1 and 7.");
        }

        employeeManager.add(name, id, bankAccount, salary, terms, startDate);
    }

    /**
     * Terminates an employee and removes their system access.
     *
     * @param id the unique ID of the employee to remove
     * @throws IllegalArgumentException if the employee is not found or is already inactive
     */

    public void remove(int id) {
        employeeManager.remove(id);   // terminate
        accessService.removeUser(id); // clean up access
    }

    public void updateName(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        emp.setName(newName);
    }

    public void updateBankAccount(int id, int newBankAccount) {
        if (newBankAccount < 0) {
            throw new IllegalArgumentException("Invalid bank account.");
        }
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        emp.setBankAccount(newBankAccount);
    }

    public void updateSalary(int id, double newSalary) {
        if (newSalary <= 0) {
            throw new IllegalArgumentException("Salary must be positive.");
        }
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        emp.setSalary(newSalary);
    }

    public void updateJobStatus(int id) {
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        emp.getTerms().changeJobStatus();
    }

    public void updateSalaryType(int id) {
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        emp.getTerms().changeSalaryType();
    }

    public void updateRestDays(int id, int days) {
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException("The number of days off must be positive..");
        }
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        emp.getTerms().setRestDays(days);
    }

    /**
     * Checks if an employee exists and returns their name.
     * @throws IllegalArgumentException if not found
     */
    public String getEmployeeName(int id) {
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        return emp.getName();
    }

    /**
     * Returns a human-readable summary of the employee's details.
     */
    public String getEmployeeDetails(int id) {
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        return emp.toString();
    }

    /**
     * Returns a human-readable summary of the employee's terms.
     */
    public String getEmployeeTermsDisplay(int id) {
        Employee emp = employeeManager.getById(id);
        if (emp == null) {
            throw new IllegalArgumentException("Employee " + id + " not found.");
        }
        return emp.getTerms().toString();
    }

    /**
     * Checks whether an employee with the given ID exists.
     * Use this instead of getEmployee() when you only need a yes/no answer.
     */
    public boolean exists(int id) {
        return employeeManager.isEmployee(id);
    }
}