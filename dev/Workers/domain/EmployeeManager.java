package dev.Workers.domain;
import java.util.*;


public class EmployeeManager implements IManager<Employee> {


    private static EmployeeManager instance;
    private List<Employee> employees;



    private EmployeeManager() {
        this.employees = new ArrayList<>();
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
    public void add(Employee employee) {
        employees.add(employee);

    }
    public void add(String name, int id, int bankAccount, double salary, String terms, Date startDate) {
        if (isEmployee(id)) {
            throw new IllegalArgumentException("Employee already work.");
        }
        if (salary <= 0) {
            throw new IllegalArgumentException("Salary must be a positive number.");
        }
        if (bankAccount <= 0) {
            throw new IllegalArgumentException("Invalid bank account details.");
        }
        Employee newEmp = new Employee(name, id, bankAccount, salary, terms, startDate);
        add(newEmp);
    }




    public Employee getById(int id){
        for (Employee employee : employees) {
            if (employee.getId() == id) {
                return employee;
            }
        }
        return null;
    }

    @Override
    public void remove(int id) {

        for (int i = 0; i < employees.size(); i++) {
            if (employees.remove(i).getId() == id) {
                //remove from list
                employees.remove(i);
            }
        }


    }


}