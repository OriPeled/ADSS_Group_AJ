package dev.Workers.domain;

<<<<<<< Updated upstream
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

=======
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftManager implements IListManager<List<Role>> {
    private static Map<Shift, List<Integer>> rolesByEmployee;

    private static ShiftManager instance;

    public static ShiftManager getInstance() {
        if (instance == null)
            instance = new ShiftManager();
        return instance;
    }

    private ShiftManager() {
        rolesByEmployee = new HashMap<>();
    }

    public void addShift(shiftType shiftType, Date date) {
        Shift shift = new Shift(shiftType, date);

    }

    @Override
    public void addFullList(int id, List<List<Role>> items) {

    }

    @Override
    public void addSingleItem(int id, List<Role> item) {

    }

    @Override
    public void removeAll(int id) {

    }

    @Override
    public void removeSingleItem(int id, List<Role> item) {

    }

    @Override
    public List<List<Role>> getListById(int id) {
        return null;
    }

    public Lis
>>>>>>> Stashed changes
}
