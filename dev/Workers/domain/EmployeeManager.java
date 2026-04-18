package dev.Workers.domain;

import dev.Workers.domain.Objects.Employee;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all employees in the system.
 * <p>
 * This class is implemented as a Singleton.
 * Provides functionality to add, remove, and retrieve employees.
 */
public class EmployeeManager implements IManager<Employee> {
    // maps employee ID to Employee object
    private Map<Integer, Employee> employees;
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
     * Adds an employee object to the system
     *
     * @param id       employee ID (not used directly, taken from employee object)
     * @param employee employee object
     */
    @Override
    public void add(int id, Employee employee) {

        employees.put(employee.getId(), employee);
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
            System.out.println("Employee already works.");
            return;
        }
        if (salary <= 0) {
            System.out.println("Salary must be a positive number.");
            return;
        }
        if (bankAccount <= 0) {
            System.out.println("Invalid bank account details.");
            return;
        }
        Employee newEmp = new Employee(name, id, bankAccount, salary, terms, startDate);
        add(id, newEmp);
    }

    @Override
    public void remove(int id) {
        Employee emp = employees.get(id);
        if (emp == null || !emp.isActive()) {
            throw new IllegalArgumentException("Employee " + id + " not found or already inactive.");
        }
        emp.terminateEmployee(LocalDate.now());
    }

    @Override
    public Employee getById(int id) {
        return employees.get(id);
    }

    public void validateEmployeeBasic(int id) {
        if (!isEmployee(id)) throw new IllegalArgumentException("Unknown ID: " + id);
        if (!getById(id).isActive()) throw new IllegalArgumentException("Employee " + id + " is inactive.");
    }
}