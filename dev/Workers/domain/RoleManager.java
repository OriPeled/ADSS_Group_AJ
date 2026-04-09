package dev.Workers.domain;
import dev.Workers.domain.Objects.Role;

import java.util.*;

public class RoleManager implements IListManager<Role> {
    private Map<Integer, List<Role>> employeeRoles;
    private String existingRoles[] = {"Cashier", "Storekeeper"};

    private static RoleManager instance;

    private RoleManager() {
        this.employeeRoles = new HashMap<>();
    }

    public static RoleManager getInstance() {
        if (instance == null)
            instance = new RoleManager();
        return instance;
    }

    @Override
    public void addFullList(int id, List<Role> items) {
        employeeRoles.put(id, items);
    }

    @Override
    public void addSingleItem(int id, Role role) {
        if (!employeeRoles.containsKey(id)) {
            employeeRoles.put(id, new ArrayList<>());
        }
        if (!employeeRoles.get(id).contains(role)) {
            employeeRoles.get(id).add(role);
        }
    }
    public List<Integer> getAllManagers() {
        List<Integer> managers = new ArrayList<>();
        for (Integer id : employeeRoles.keySet()) {
            List<Role> roles = getListById(id);
            for (Role role : roles){
               if (role.getRolename()=="Manager") {
                   managers.add(id);
               }
            }

        }
        return managers;
    }

    @Override
    public void removeAll(int id) {
        employeeRoles.remove(id);
    }

    @Override
    public void removeSingleItem(int id, Role role) {
        if (employeeRoles.containsKey(id)) {
            employeeRoles.get(id).remove(role);

            if (employeeRoles.get(id).isEmpty()) {
                employeeRoles.remove(id);
            }
        }
    }

    @Override
    public List<Role> getListById(int id) {
        return employeeRoles.getOrDefault(id, new ArrayList<>());
    }

    public void addRole(String role) {
        String[] arr = Arrays.copyOf(existingRoles, existingRoles.length + 1);
        arr[existingRoles.length - 1] = role;
    }

    public List<Integer> getListByRole(Role role) {
        List<Integer> qualifiedEmployees = new ArrayList<>();
        for (Integer id : employeeRoles.keySet()){
            List<Role> roles = getListById(id);
            if (roles.contains(role)) {
                qualifiedEmployees.add(id);
            }

            }
        return qualifiedEmployees;
    }

    public String[] getExistingRoles() {
        return existingRoles;
    }
}
