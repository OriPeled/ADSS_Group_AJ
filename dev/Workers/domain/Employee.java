package dev.Workers.domain;

import java.time.LocalDate;

public class Employee {
    private String name;
    private int id;
    private int bankAccount;
    private double salary;
    private String terms;
    private LocalDate startDate;
 //   private List<Role> certifiedRoles;
 // private List<Constraint>


    public Employee(String name, int id, int bankAccount, double salary, String terms, LocalDate startDate) {
        this.name = name;
        this.id = id;
        this.bankAccount = bankAccount;
        this.salary = salary;
        this.terms = terms;
        this.startDate = startDate;
       // this.certifiedRoles = new ArrayList<>();
    }

  //  public void AddRole(Role role) {
   //     if (role != null) {
   //         this.certifiedRoles.add(role);
    //    }
   // }

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

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public LocalDate getStartLocalDate() {
        return startDate;
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

    //   public List<Role> getCreditRoles() {
      //  return certifiedRoles;
   // }
}
