package dev.Workers.Service;


import dev.Workers.domain.Enums.LicenseType;
import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.EmployeeManager;
import dev.Workers.domain.Objects.EmployeeTerms;

import java.time.DayOfWeek;
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
    public void add(String name, int id, LicenseType licenseType, int bankAccount, double salary, EmployeeTerms terms,
                        LocalDate startDate) {
        // 1. Business logic validations
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cannot add employee: Name cannot be empty.");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("Cannot add employee: ID must be a positive number.");
        }
        if (licenseType == null) {
            throw new IllegalArgumentException("Cannot add employee: invalid license type.");
        }
        if (terms == null) {
            throw new IllegalArgumentException("Cannot add employee: Employment terms cannot be null.");
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

        employeeManager.add(name, id, licenseType, bankAccount, salary, terms, startDate);
    }

    /**
     * Terminates an employee and removes their system access.
     *
     * @param id the unique ID of the employee to remove
     * @throws IllegalArgumentException if the employee is not found or is already inactive
     */

    public void fire(int id) {
        employeeManager.fire(id);   // terminate
        accessService.removeUser(id); // clean up access
    }

    public void updateName(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);
        emp.setName(newName);
    }

    public void updateLicenseType(int empId, LicenseType licenseType) {
        employeeManager.validateEmployeeBasic(empId, LocalDate.now());
        Employee emp = employeeManager.getById(empId);
        emp.setLicenseType(licenseType);
    }

    public void updateBankAccount(int id, int newBankAccount) {
        if (newBankAccount < 0) {
            throw new IllegalArgumentException("Invalid bank account.");
        }
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);
        emp.setBankAccount(newBankAccount);
    }

    public void updateSalary(int id, double newSalary) {
        if (newSalary <= 0) {
            throw new IllegalArgumentException("Salary must be positive.");
        }
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);
        emp.setSalary(newSalary);
    }

    public void updateJobStatus(int id) {
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);
        emp.getTerms().changeJobStatus();
    }

    public void updateSalaryType(int id) {
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);
        emp.getTerms().changeSalaryType();
    }

    public void updateRestDays(int id, int days) {
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException("The number of days off must be positive..");
        }
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);
        emp.getTerms().setRestDays(days);
    }

    public void updateDayOff(int id, DayOfWeek day) {
        employeeManager.validateEmployeeBasic(id, LocalDate.now());
        Employee emp = employeeManager.getById(id);
        emp.getTerms().setDayOff(day);
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

    public void rehire(int empId) {
        employeeManager.rehire(empId);
    }

    public Employee getById(int i) {
        return employeeManager.getById(i);
    }

    public UserResponse promoteDemote(int empId) {
        return employeeManager.promoteDemote(empId);
    }
}