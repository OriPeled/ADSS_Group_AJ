package dev.Workers.domain;

import java.time.LocalDate;

public class Employee {
    private String name;
    private int id;
    private int bankAccount;
    private double salary;
    private EmployeeTerms terms;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isManager;
    // private UserDetails userDetails;
 //   private List<Role> certifiedRoles;
 // private List<Constraint>


    public Employee(String name, int id, int bankAccount, double salary, EmployeeTerms terms, LocalDate startDate) {
        this.name = name;
        this.id = id;
        this.bankAccount = bankAccount;
        this.salary = salary;
        this.terms = terms;
        this.startDate = startDate;
        this.endDate = null;
        this.isManager = false;
       // this.certifiedRoles = new ArrayList<>();
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(int bankAccount) {
        this.bankAccount = bankAccount;
    }

    public int getId() {
        return id;
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

    public void promoteDemote() {
        if (isManager)
            isManager = false;
            System.out.println(name + " is no longer shift manager.");
        isManager = true;
        System.out.println(name + " is now shift manager.");
    }

    @Override
    public String toString() {
        return "Employee{" +
                ",  ID: " + id +
                " 1. Name: '" + name + '\'' +
                ", 2. Bank account: " + bankAccount +
                ", 3. Salary: " + salary +
                ", 4. Terms: '" + terms + '\'' +
                ",  Start date: " + startDate +
                '}';
    }
    public LocalDate getEndLocalDate() {
        return endDate;
    }
    public void terminateEmployee(LocalDate terminationDate) {
        this.endDate = terminationDate;
    }
    public boolean isActive() {
        return endDate == null;
    }

}
