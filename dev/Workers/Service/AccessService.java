package dev.Workers.Service;

import dev.Workers.domain.Access;
import dev.Workers.domain.Employee;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AccessService {

    private static Map<Employee, Access> accessMap;
    private static AccessService instance;

    private AccessService() {
        this.accessMap = new HashMap<>();
    }

    public static AccessService getInstance() {
        if (instance == null) {
            instance = new AccessService();
        }
        return instance;
    }

    public void Register(int id,  String password) {

        if (isPasswordEmpty(password)){
            throw new IllegalArgumentException("Password cannot be empty");
        }
        else
        {
            for(Employee employee: accessMap.keySet()) {
                if (employee.getId() == id) {
                    if (!employee.isActive()) {
                        throw new IllegalArgumentException("Employee is not active");
                    } else {
                        accessMap.put(employee, new Access(password));
                    }
                }
            }
        }

    }

    public void Remove(Employee employee) {
        if (!employee.isActive()) {
            accessMap.remove(employee);
        } else {
            throw new IllegalArgumentException("Employee is active");
        }
    }

    public void updatePassword(Employee employee, String password) {
        if (!employee.isActive()) {
            throw new IllegalArgumentException("Employee is not active");
        } else {
            Access access = accessMap.get(employee);
            if (access != null) {
                access.setPassword(password);
            } else {
                throw new IllegalArgumentException("Employee does not have access");
            }
        }
    }

    public boolean isRegisteredUser(int employeeID) {
        for (Employee employee : accessMap.keySet()) {
            if (employee.getId() == employeeID ) {
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordEmpty(String password) {

        return password.equals("") || password ==null;
    }

}



