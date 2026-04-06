package dev.Workers.domain;



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

}
