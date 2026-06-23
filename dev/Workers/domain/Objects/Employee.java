package dev.Workers.domain.Objects;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an employee in the system.
 *
 * Each employee has personal details, employment terms,
 * salary information, and employment status (active/terminated).
 */
public class Employee {
    private String name;
    private int id;
    private Branch branch;
    private boolean isManager;
    private int bankAccount;
    private double salary;
    private EmployeeTerms terms;        // jobStatus, salary type, rest days
    private LocalDate startDate;
    private LocalDate endDate;          // updated if fired

    private final Set<Role> assignedRoles;
    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public Employee(String name, int id, Branch branch, int bankAccount, double salary,
                        EmployeeTerms terms, LocalDate startDate) {
        this.name = name;
        this.id = id;
        this.branch = branch;
        this.isManager = false;
        this.bankAccount = bankAccount;
        this.salary = salary;
        this.terms = terms;
        this.startDate = startDate;
        this.endDate = null;
        this.assignedRoles = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public int getId() {
        return id;
    }

    public int getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(int bankAccount) {
        this.bankAccount = bankAccount;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public EmployeeTerms getTerms() {
        return terms;
    }

    public void setTerms(EmployeeTerms terms) {
        this.terms = terms;
    }

    public LocalDate getStartLocalDate() {
        return startDate;
    }

    public Set<Role> getRoles() {
        return assignedRoles;
    }

    public boolean hasRole(Role role) {
        return assignedRoles.contains(role);
    }

    public void addRole(Role role) {
        assignedRoles.add(role);
    }

    public void removeRole(Role role) {
        if (!hasRole(role))
            throw new IllegalArgumentException("Employee doesn't have this role.");
        assignedRoles.remove(role);     // this works? equals for Role?
    }

    public void removeAllRoles() {
        assignedRoles.clear();
    }

    public boolean belongsToBranch(Branch branch) {
        return this.branch == branch;
    }

    /**
     * @return employment end date ,null if still active
     */
    public LocalDate getEndLocalDate() {return endDate;}

    /**
     * Sets the employment end date directly.
     * Used only by the persistence layer (DAO) to restore an employee's exact
     * state from the database. Business code should use terminateEmployee /
     * activateEmployee instead, which apply the termination notice logic.
     *
     * @param endDate the end date to restore (null means active)
     */
    public void setEndLocalDate(LocalDate endDate) { this.endDate = endDate; }

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
    public boolean isActive(LocalDate date) {
        boolean hasStarted = !date.isBefore(startDate);
        boolean hasNotEnded = (endDate == null) || !date.isAfter(endDate);

        return hasStarted && hasNotEnded;
    }
    public boolean toBeActive() {
        return startDate.isAfter(LocalDate.now());
    }

    public boolean isTerminated() { return endDate!= null; }

    public String status() {
        if (toBeActive()) {
            return "[ACTIVE from " + startDate + "]";
        }

        if (isTerminated()) {
            if (endDate.isBefore(LocalDate.now())) {
                return "[INACTIVE]";
            } else {
                return "[ACTIVE until " + endDate + "]";
            }
        }

        return "[ACTIVE]";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Employee Details\n");
        sb.append("======================================\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Branch: ").append(branch.getName()).append("\n");
        sb.append("Bank account: ").append(bankAccount).append("\n");
        sb.append("Salary: ").append(salary).append("\n");
        sb.append("Terms:\n   ")
                .append(terms.toString().replace("\n", "\n   "))
                .append("\n");

        sb.append("Start date: ").append(startDate).append("\n");
        sb.append("Is manager: ").append(isManager);

        if (endDate != null) {
            sb.append("\nEnd date: ").append(endDate);
        }

        return sb.toString();
    }

}
