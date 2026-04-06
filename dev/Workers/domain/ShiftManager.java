package dev.Workers.domain;

import java.time.LocalDate;
import java.util.*;

import static dev.Workers.domain.shiftType.evening;
import static dev.Workers.domain.shiftType.morning;

public class ShiftManager implements IListManager<List<Role>> {
    //private static Map<Shift, Map<Role, List<Employee>>> shifts;
    private static List<Shift> shifts;
    private static Map<Role, List<Integer>> rolesByEmployeeID;

    private static ShiftManager instance;

    public static ShiftManager getInstance() {
        if (instance == null)
            instance = new ShiftManager();
        return instance;
    }

    private ShiftManager() {
        shifts = new ArrayList<>();
        rolesByEmployeeID = new HashMap<>();
    }

    public void addEmployeeToShift(Shift shift, int id) {

    }

    public void addShift(shiftType shiftType, Date date) {
        if (shiftType != morning || shiftType != evening)
            System.out.println("Invalid shift type.");
        if (!(date instanceof Date))
            System.out.println("Invalid date format.");
        Shift shift = new Shift(shiftType, date);
        if (!shifts.contains(shift))
            shifts.add(shift);
    }

    public void removeShift(Shift shift) {
        if (!shifts.contains(shift))
            System.out.println("Shift doesn't exist.");
        shifts.remove(shift);
    }

    public Shift getShift(LocalDate date, String shiftTime) {
        // TODO
        shiftType shiftType1 = shiftType.valueOf(shiftTime);
        return null;
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
}
