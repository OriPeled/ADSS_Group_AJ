package dev.Workers.domain.Objects;

import dev.Workers.domain.EmployeeTerms;

import java.time.LocalDate;

/**
 * Represents an employee in the system.
 *
 * Each employee has personal details, employment terms,
 * salary information, and employment status (active/terminated).
 */
public class Employee {
    // employee full name
    private String name;
    // employee id
    private int id;
    // employee bank Account
    private int bankAccount;
    // employee salary
    private double salary;
    // employee terms- jobStatus ,salary Type , rest Days
    private EmployeeTerms terms;
    // employee start Date
    private LocalDate startDate;
    // employee end Date
    private LocalDate endDate;
    // checking if manger or HR
    //private boolean isManager;
    // private UserDetails userDetails;
 //   private List<Role> certifiedRoles;
 // private List<Constraint>

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
       // this.certifiedRoles = new ArrayList<>();
    }
    /**
     * Copy constructor - creates a deep copy of another Employee
     *
     * @param other the employee to copy
     */
    public Employee(Employee other) {
        this.name = other.name;
        this.id = other.id;
        this.bankAccount = other.bankAccount;
        this.salary = other.salary;
        this.terms = other.terms;
        this.startDate = other.startDate;
        this.endDate = other.endDate;
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
        return "Employee Details" +
                "\nID: " + id +
                "\n1. Name: " + name +
                "\n2. Bank account: " + bankAccount +
                "\n3. Salary: " + salary +
                "\n4. Terms: " + terms +
                "\nStart date: " + startDate;
    }
    /**
     * @return employment end date ,null if still active
     */
    public LocalDate getEndLocalDate() {return endDate;}
    /**
     * Terminates employee by setting end date
     * @param terminationDate date of termination
     */
    public void terminateEmployee(LocalDate terminationDate) {
        this.endDate = terminationDate;
    }
    /**
     * Checks if employee is still active
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return endDate == null;
    }

}
