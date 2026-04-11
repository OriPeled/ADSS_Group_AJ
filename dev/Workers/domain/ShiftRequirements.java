package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Shift;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static dev.Workers.domain.Enums.Role.shiftManager;

/**
 * Manages required staffing levels for shifts.
 *
 * This class tracks how many employees are required per role
 * for each shift in the system.
 */
public class ShiftRequirements {

    private static Map<Shift, Map<Role, Integer>> Requirements;
    /**
     * Initializes the requirements storage
     */
    public ShiftRequirements() {
        Requirements = new HashMap<>();
    }

    /**
     * Internal method for initializing a shift requirement entry
     */
    private void add(Shift shift, Role role, int count) {
        // If this shift does not exist yet in the map,
        // initialize a new inner map for roles -> required count
        if(!Requirements.containsKey(shift)){
            Map<Role, Integer> newInnerMap = new HashMap<>();
            // Default rule: every shift must have at least 1 Manager
            newInnerMap.put(shiftManager, 1);
            Requirements.put(shift, newInnerMap);
        }
        // Business rule: Manager requirement cannot go below 1
        if (role==shiftManager && count < 1) {
            count = 1;
        }
        // Get current required count for this role in the shift
        int tot = Requirements.get(shift).get(role);
        // Update requirement by subtracting the given count
        tot = tot - count;
        // Save updated requirement back into the map
        Requirements.get(shift).put(role, tot);
    }
    /**
     * Updates the required number of employees for a role in a shift
     *
     * @param shift the shift
     * @param role the role
     * @param count number of employees (delta or required value depending on logic)
     */
    public void update(Shift shift, Role role, int count) {
        if (count < 0) {
            System.out.println("Count must be a non-negative number.");
        }
        if (role==shiftManager && count == 0) {
           System.out.println("There must be one manager on shift.");
        }
        if (!Requirements.containsKey(shift)){
            add(shift, role, count);
            return;
        }
        if (count == 0) {
            Requirements.get(shift).remove(role);
        }
        else {
            Requirements.get(shift).put(role, count);
        }
    }
    /**
     * Removes all requirements for a shift
     *
     * @param shift shift to remove
     */
    public void remove(Shift shift){
        if (!Requirements.containsKey(shift)) {
            System.out.println("Shift does not exist.");
        }
        Requirements.remove(shift);
    }
    /**
     * Returns required employee count for a role in a shift
     *
     * @param shift shift
     * @param role role
     * @return required number of employees
     */
    public int getCountByRole(Shift shift, Role role){
        if (!Requirements.containsKey(shift)) {
            System.out.println("Shift does not exist.");
        }
        return Requirements.get(shift).get(role);
    }
    /**
     * Finds requirements for a shift by date and type
     *
     * @param shiftDate date of shift
     * @param shiftT shift type
     * @return role requirements map or null if not found
     */
    public static Map<Role, Integer> requirementsByDate(LocalDate shiftDate, shiftType shiftT) {
        for (Shift shift : Requirements.keySet()) {
            if (shift.getShiftDate() == shiftDate && shift.getShift() == shiftT) {
                return Requirements.get(shift);
            }
        }
        return null;
    }
    /**
     * @return string representation of all shift requirements
     */
    @Override
    public String toString() {
        return "ShiftRequirements{" +
                "Requirements=" + Requirements +
                '}';
    }
}






