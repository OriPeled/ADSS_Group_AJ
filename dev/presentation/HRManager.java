package dev.presentation;

import dev.domain.Employee;
import dev.domain.Role;
import dev.domain.Shift;

import java.util.List;
import java.util.Map;

public class HRManager {
    private List<Employee> employee;
    private Map<Shift, List<Employee>> shifts;
    private List<Role> availableRoles;
    private Map<Role, Integer> requirements;
    private  Map<Role, List<Employee>> employees;
}
