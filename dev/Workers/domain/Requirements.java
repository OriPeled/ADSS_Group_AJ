package dev.Workers.domain;

import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Objects.Shift;

import java.util.*;

/**
 * Stores required number of employees per role for each shift.
 *
 * Structure:
 * Shift -> Role -> Required count
 */
public class Requirements {
    private final Map<Shift, Map<Role, Integer>> data; // shift to (role to required amount)     //

    public Requirements() {
        this.data = new HashMap<>();
    }

    // default: 3
    public void init(Shift shift) {
        Map<Role, Integer> innerMap = new HashMap<>();
        //innerMap.put(Role.shiftManager, 1);
        for (Role role : Role.values()) {
            //if (role != Role.shiftManager) {
                innerMap.put(role, 3);
            //}

        }
        data.put(shift, innerMap);
    }

    /**
     * Sets required number of employees for a role in a shift.
     *
     * @param shift the shift
     * @param role the role
     * @param count required number of employees (must be >= 0)
     */
    public void set(Shift shift, Role role, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }

        Map<Role, Integer> shiftMap =
                data.computeIfAbsent(shift, k -> new HashMap<>());

        if (count == 0) {
            shiftMap.remove(role);
        } else {
            shiftMap.put(role, count);
        }

        if (shiftMap.isEmpty()) {
            data.remove(shift);
        }
    }

    /**
     * Gets required number of employees for a role in a shift.
     *
     * @param shift the shift
     * @param role the role
     * @return required count (0 if not defined)
     */
    public int countRequired(Shift shift, Role role) {
        return data
                .getOrDefault(shift, Collections.emptyMap())
                .getOrDefault(role, 0);
    }

    /**
     * Removes a role requirement from a shift.
     */
    public void remove(Shift shift, Role role) {
        Map<Role, Integer> shiftMap = data.get(shift);
        if (shiftMap == null) return;

        shiftMap.remove(role);

        if (shiftMap.isEmpty()) {
            data.remove(shift);
        }
    }

    /**
     * Returns full requirements map (read-only copy).
     */
    public Map<Shift, Map<Role, Integer>> getAll() {
        return new HashMap<>(data);
    }
    @Override
    public String toString() {
        if (data.isEmpty()) {
            return "No requirements defined.";
        }
        String result = "Shift Requirements:\n";
        for (Map.Entry<Shift, Map<Role, Integer>> shiftEntry : data.entrySet()) {
            Shift shift = shiftEntry.getKey();
            Map<Role, Integer> roles = shiftEntry.getValue();
            result += "Shift: " + shift + " | ";
            boolean first = true;
            for (Map.Entry<Role, Integer> roleEntry : roles.entrySet()) {
                if (!first) {
                    result += ", ";
                }

                result += roleEntry.getKey() + ": " + roleEntry.getValue();
                first = false;
            }
            result += "\n";
        }

        return result;
    }
}