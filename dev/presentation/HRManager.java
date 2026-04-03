package dev.presentation;

import dev.domain.Employee;
import dev.domain.Role;
import dev.domain.Shift;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class HRManager {
    private List<Employee> employees;
    private Map<Shift, Map<Role, List<Employee>>> shifts;
    private List<Role> availableRoles;
    //private Map<Role, Integer> requirements;
    private Map<Role, List<Employee>> employeeRoles;

    public void addEmployee(String name, int id, int bankAccount, double salary, String terms, Date startDate) {
        Employee employee = new Employee(name, id, bankAccount, salary, terms, startDate);
        if (!checkIfExists(id))
            employees.add(employee);
    }

    public void removeEmployee(int id) {
        if (checkIfExists(id))
            for (Employee employee : employees) {
                if (employee.getId() == id)
                    employees.remove(employee);
            }
    }

    public boolean checkIfExists(int id) {
        for (Employee employee : employees) {
            if (employee.getId() == id)
            return true;
        }
        return false;
    }
}
