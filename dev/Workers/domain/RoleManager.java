package dev.Workers.domain;
import java.util.*;

public class RoleManager implements IListManager<Role> {
    private Map<Integer, List<Role>> employeeRoles = new HashMap<>();


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
    }
