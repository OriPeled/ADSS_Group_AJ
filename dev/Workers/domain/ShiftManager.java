package dev.Workers.domain;

import java.time.LocalDate;
import java.util.*;

import static dev.Workers.domain.shiftType.evening;
import static dev.Workers.domain.shiftType.morning;

public class ShiftManager   {
    private static Set<Shift> shifts;

    private static ShiftManager instance;

    public static ShiftManager getInstance() {
        if (instance == null)
            instance = new ShiftManager();
        return instance;
    }

    private ShiftManager() {
        shifts = new HashSet<>();
    }

    public void assignToShift(Shift shift, Integer IDs[]) {
        for (int id: IDs) {

        }
    }

    public void addShift(LocalDate date, String shiftTypeString) {
        if (date == null)
            System.out.println("Invalid date format.");
        if (shiftTypeString != "morning" && shiftTypeString != "evening")
            System.out.println("Invalid shift type.");
        Shift shift = new Shift(date, shiftTypeString);
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

    /**
     *
     * @return history of shifts
     */
    public String getShiftsHistory() {
        StringBuilder sb = new StringBuilder();
        if (shifts.isEmpty()) {
            sb.append("No shifts recorded.\n");
        } else {
            for (Shift s : shifts) {
                sb.append(s.toString()).append("\n");
            }
        }
        return sb.toString();
    }

}
