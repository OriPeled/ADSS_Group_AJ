package dev.Workers.domain;

import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Constraint;

import java.time.DayOfWeek;
import java.util.*;

public class ConstraintManager  {
    private static ConstraintManager instance;

    public Map<Integer, Constraint> getEmployeeConstraints() {
        return employeeConstraints;
    }

    private Map<Integer, Constraint> employeeConstraints;

    private ConstraintManager() {
        this.employeeConstraints = new HashMap<>();

    }

    public static ConstraintManager getInstance() {
        if (instance == null) {
            instance = new ConstraintManager();
        }
        return instance;
    }
    public Constraint display(int id) {
        return employeeConstraints.get(id);
    }
    public void update(int id, DayOfWeek day, shiftType shiftType) {

        Constraint employeeConstraints =display(id);
        employeeConstraints.getWeekConstraints().put(day, shiftType);
    }




}