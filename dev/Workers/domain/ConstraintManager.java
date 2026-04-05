package dev.Workers.domain;

import java.util.*;

public class ConstraintManager implements IListManager<Constraint> {

    private static ConstraintManager instance;
    private Map<Integer, List<Constraint>> employeeConstraints;

    private ConstraintManager() {
        this.employeeConstraints = new HashMap<>();
    }

    public static ConstraintManager getInstance() {
        if (instance == null) {
            instance = new ConstraintManager();
        }
        return instance;
    }

    @Override
    public void addFullList(int id, List<Constraint> items) {
        employeeConstraints.put(id, new ArrayList<>(items));
    }

    @Override
    public void addSingleItem(int id, Constraint item) {
        employeeConstraints.computeIfAbsent(id, k -> new ArrayList<>());
        List<Constraint> constraints = employeeConstraints.get(id);
        if (!constraints.contains(item)) {
            constraints.add(item);
        }
    }

    @Override
    public void removeAll(int id) {
        if (employeeConstraints.containsKey(id)) {
            employeeConstraints.remove(id);
        } else {
            throw new NoSuchElementException("No Constraints found for employee ID: " + id);
        }
    }

    @Override
    public void removeSingleItem(int id, Constraint item) {
        if (employeeConstraints.containsKey(id)) {
            List<Constraint> constraints = employeeConstraints.get(id);
            constraints.remove(item);

            if (constraints.isEmpty()) {
                employeeConstraints.remove(id);
            }
        } else {
            throw new NoSuchElementException("Employee ID " + id + " not found in constraint records.");
        }
    }

    @Override
    public List<Constraint> getListById(int id) {
        return new ArrayList<>(employeeConstraints.getOrDefault(id, new ArrayList<>()));
    }
    public List<Integer> getEmployeesByConstraint(Constraint constraint){
        List<Integer> qualifiedEmployees = new ArrayList<>();
        for (Integer id : employeeConstraints.keySet()) {
            List<Constraint> constraints = employeeConstraints.get(id);
            if (constraints.contains(constraint)){
                qualifiedEmployees.add(id);
            }

            }
        return qualifiedEmployees;
        }
    }

