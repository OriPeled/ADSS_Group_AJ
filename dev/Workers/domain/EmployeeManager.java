package dev.Workers.domain;
import dev.Workers.domain.Objects.Employee;
import dev.Workers.domain.Objects.EmployeeTerms;

import java.time.LocalDate;
import java.util.*;


public class EmployeeManager implements IManager<Employee> {
    private Map<Integer, Employee> employees;

    private static EmployeeManager instance;

    private EmployeeManager() {
        this.employees = new HashMap<>();
    }

    public static EmployeeManager getInstance() {
        if (instance == null) {
            instance = new EmployeeManager();
        }
        return instance;
    }
    public boolean isEmployee(int id) {
        return getById(id) != null;
    }

    @Override
    public void add(int id,Employee employee) {
        employees.put(employee.getId(), employee);
    }

    public void add(String name, int id, int bankAccount, double salary, EmployeeTerms terms, LocalDate startDate) {
        if (isEmployee(id)) {
            System.out.println("Employee already works.");
            }
        if (salary <= 0) {
            System.out.println("Salary must be a positive number.");
            }
        if (bankAccount <= 0) {
            System.out.println("Invalid bank account details.");
            }
        Employee newEmp = new Employee( name,  id,  bankAccount,  salary,  terms,  startDate);
        add(id,newEmp);
    }

    public Employee getById(int id) {
        return employees.get(id);
    }

    public boolean wrongPassword(String password) {
        // TODO
        return false;
    }

    public boolean isRegisteredUser(int enteredID) {
        // TODO
        return false;
    }

    public void setPassword(String nextLine) {
    }

   @Override
   public void remove(int id) {
       Employee emp = employees.get(id);
       if (emp.isActive()) {
           emp.terminateEmployee(LocalDate.now());

     } else {
            System.out.println("Employee ID " + id + " not active.");
        }
    }


}