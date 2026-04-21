package dev.Workers.domain.Objects;

import java.time.LocalDate;

/**
 * Represents an employee in the system.
 *
 * Each employee has personal details, employment terms,
 * salary information, and employment status (active/terminated).
 */
public class Employee {
    private String name;
    private int id;
    private int bankAccount;
    private double salary;
    private EmployeeTerms terms;        // jobStatus, salary type, rest days
    private LocalDate startDate;
    private LocalDate endDate;          // updated if fired

    private boolean isManager;
    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }


    /**
     * Constructor for creating a new employee.
     *
     * @param name employee name
     * @param id employee ID
     * @param bankAccount bank account number
     * @param salary employee salary
     * @param terms employment terms
     * @param startDate employment start date
     */
    public Employee(String name, int id, int bankAccount, double salary, EmployeeTerms terms, LocalDate startDate) {
        this.name = name;
        this.id = id;
        this.bankAccount = bankAccount;
        this.salary = salary;
        this.terms = terms;
        this.startDate = startDate;
        this.endDate = null;
        this.isManager = false;
    }

    /**
     * getter for name
     * @return employee name
     */

    public String getName() {
        return name;
    }
    /**
     * Updates employee name
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * getter for Bank Account
     * @return the number of bank Account
     */
    public int getBankAccount() {
        return bankAccount;
    }
    /**
     * Updates bank account number
     * @param bankAccount new bank account
     */
    public void setBankAccount(int bankAccount) {
        this.bankAccount = bankAccount;
    }
    /**
     * @return employee ID
     */
    public int getId() {
        return id;
    }

    /**
    * @return employee salary
    */
    public double getSalary() {
        return salary;
    }
    /**
     * Updates employee salary
     * @param salary new salary
     */
    public void setSalary(double salary) {
        this.salary = salary;
    }
    /**
     * @return employment terms
     */
    public EmployeeTerms getTerms() {
        return terms;
    }
    /**
     * Updates employment terms
     * @param terms new terms
     */
    public void setTerms(EmployeeTerms terms) {
        this.terms = terms;
    }
    /**
     * @return employee start date
     */
    public LocalDate getStartLocalDate() {
        return startDate;
    }

    /**
     * @return string representation of employee details
     */
    @Override
    public String toString() {
        String result = "Employee Details" +
                "\nID: " + id +
                "\n1. Name: " + name +
                "\n2. Bank account: " + bankAccount +
                "\n3. Salary: " + salary +
                "\n4. Terms: " + terms +
                "\nStart date: " + startDate +
                "\nIs manager: " + isManager;

        if (endDate != null) {
            result += "\nEnd date: " + endDate;
        }

        return result;
    }

    /**
     * @return employment end date ,null if still active
     */
    public LocalDate getEndLocalDate() {return endDate;}

    /**
     * Terminates employee by setting end date a week in advance.
     * @param terminationDate date of termination
     */
    public void terminateEmployee(LocalDate terminationDate) {
        this.endDate = terminationDate.plusDays(7);
    }

    public void activateEmployee() {
        endDate = null;
    }

    /**
     * Checks if employee is still active
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return endDate == null;
    }

}
