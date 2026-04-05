package dev.Workers.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleManager implements IManager<Role>{
    private static RoleManager instance;
    private Map<Integer, List<Role>> employeeRoles;

    private RoleManager() {
        this.employeeRoles = new HashMap<>();
    }
    public static RoleManager getInstance() {
        if (instance == null) {
            instance = new RoleManager();
        }
        return instance;
    }
    @Override
    public void add(Role employee) {

    }

    @Override
    public void remove(int id) {

    }

    @Override
    public Role getById(int id) {
        return null;
    }
}
