package dev.presentation;

import dev.domain.Employee;
import dev.domain.Role;
import dev.domain.Shift;

import java.util.*;

public class HRManager {

    private Map<Shift, Map<Role, List<Employee>>> shifts;
    private List<Role> availableRoles;
    //private Map<Role, Integer> requirements;
    private Map<Role, List<Employee>> EmployeesByRole;
    private Map<Employee, List<Role>> RolesOfEmployee;

    public HRManager() {
        this.availableRoles = new ArrayList<>();
        this.EmployeesByRole = new HashMap<>();
        this.RolesOfEmployee = new HashMap<>();
        this.shifts = new HashMap<>();
    }

    public void addEmployee(String name, int id, int bankAccount, double salary, String terms, Date startDate) {
        Employee employee = new Employee(name, id, bankAccount, salary, terms, startDate);
        if (!checkIfExists(id)){
            RolesOfEmployee.put(employee, new ArrayList<>());
        }
    }

    public void addRole(Role role) {
        if (role == null) return;
        if (!availableRoles.contains(role)) {
            availableRoles.add(role);
        }
            if (!EmployeesByRole.containsKey(role)) {
            EmployeesByRole.put(role, new ArrayList<>());
        }
    }

    public void addRoleToEmployee(Employee employee,Role role) {
        if(role==null || employee==null) {
            return;
        }
        if(!checkIfExists(employee.getId())) {

        }
    }
    public void addRoleToEmployee(int employeeId, Role role) {
        if (role == null) return;


        Employee employee = null;
        for (Employee e : RolesOfEmployee.keySet()) {
            if (e.getId() == employeeId) {
                employee = e;
                break;
            }
        }


        if (employee == null) {
            return;
        }


        List<Role> rolesOfE = RolesOfEmployee.get(employee);
        if (rolesOfE != null && !rolesOfE.contains(role)) {
            rolesOfE.add(role);
        }


        EmployeesByRole.computeIfAbsent(role, k -> new ArrayList<>());
        List<Employee> empsForRole = EmployeesByRole.get(role);

        if (!empsForRole.contains(employee)) {
            empsForRole.add(employee);
        }
    }
    public void removeEmployee(int id) {
        // מציאת העובד להסרה
        Employee toRemove = null;
        for (Employee e : RolesOfEmployee.keySet()) {
            if (e.getId() == id) {
                toRemove = e;
                break;
            }
        }

        if (toRemove != null) {
            RolesOfEmployee.remove(toRemove);


            for (List<Employee> list : EmployeesByRole.values()) {
                list.remove(toRemove);
            }
        }
    }

    public boolean checkIfExists(int id) {
        for (Employee employee : RolesOfEmployee.keySet()) {
            if (employee.getId() == id)
            return true;
        }
        return false;
    }
}
