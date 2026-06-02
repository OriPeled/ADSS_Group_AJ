package dev.Workers.domain.Actions;

import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.ShiftHandler;

public interface RequestAction {
    void execute(ShiftHandler manager);
    String getDescription();
    Shift shift();

    // Nested Record 1
    record AssignAction(Shift shift, Role role, int empId) implements RequestAction {
        @Override
        public void execute(ShiftHandler manager) {
            manager.forceAssign(shift, role, empId);
        }
        @Override
        public String getDescription() {
            return "Shift: " + shift.getDate() + " - " + shift.toStringByWeekDay() + "\n" +
                    "Assignment: " + role + " on " + shift.getDate();
        }
    }

    // Nested Record 2
    record ReplaceAction(Shift shift, int curId, int newId) implements RequestAction {
        @Override
        public void execute(ShiftHandler manager) {
            manager.forceReplace(shift, curId, newId);
        }
        @Override
        public String getDescription() {
            return "Shift: " + shift.getDate() + " - " + shift.toStringByWeekDay() + "\n" +
                    "Replacement: ID " + curId + " -> ID " + newId;
        }
    }
}