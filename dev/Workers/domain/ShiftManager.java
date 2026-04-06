package dev.Workers.domain;

import java.util.*;

public class ShiftManager {

    private static ShiftManager instance;
    private Map<Shift, List<Integer>> shiftAssignments;

    private ShiftManager() {
        shiftAssignments = new HashMap<>();

    }
    public static ShiftManager getInstance() {
        if (instance == null) instance = new ShiftManager();
        return instance;
    }

    public void addEmployeeToShift(Shift shift, int id) {
        shiftAssignments.computeIfAbsent(shift, k -> new ArrayList<>()).add(id);
    }
    public void removeEmployeeFromShift(Shift shift, int id) {
        shiftAssignments.computeIfAbsent(shift, k -> new ArrayList<>()).remove(id);

    }
    public List<Shift> getShiftHistory() {
        List<Shift> history = new ArrayList<>(shiftAssignments.keySet());
        return history;
    }
   // public boolean isShiftValid(Shift shift) {
   // }

}
