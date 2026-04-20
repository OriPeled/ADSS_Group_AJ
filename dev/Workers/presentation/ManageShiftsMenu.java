package dev.Workers.presentation;

import dev.Workers.Service.ConstraintService;
import dev.Workers.Service.ShiftService;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Shift;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static dev.Workers.domain.Enums.ShiftType.evening;
import static dev.Workers.domain.Enums.ShiftType.morning;
import static dev.Workers.domain.Enums.WeekStatus.READY_TO_PUBLISH;
import static dev.Workers.presentation.Main.scanner;
import static dev.Workers.presentation.Parser.getDateOfNextWeekFromDayNumber;
import static dev.Workers.presentation.Parser.getRoleFromNumber;

/**
 * Handles the HR shift-management user interface.
 *
 * This class is responsible for:
 *
 *     Managing the weekly shifts menu
 *     Selecting a day and shift type
 *     Updating shift assignments
 *     Updating staffing requirements
 *     Removing shifts
 *     Publishing the weekly schedule
 *     Updating the constraints deadline
 *     Displaying shifts history
 *
 *
 * This class belongs to the presentation layer and communicates only
 * with the relevant services.
 */
public class ManageShiftsMenu {

    /** Service responsible for all shift-related operations. */
    private static final ShiftService shiftService = ShiftService.getInstance();

    /** Service responsible for employee constraints and deadline management. */
    private static final ConstraintService constraintService = ConstraintService.getInstance();

    /**
     * Starts the main shifts menu loop.
     *
     * The user can:
     *
     *    Manage the shifts of the week
     *     View shifts history
     *     Update the constraints deadline
     *     Return to the previous menu
     *
     */
    public static void start() {
        while (true) {
            printMainMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> manageShiftsWeek();
                case 2 -> showShiftsHistory();
                case 3 -> updateDeadline();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Manages the weekly shifts flow.
     *
     * This method:
     *
     *     Checks whether the week is ready to publish
     *     Displays the current weekly assignments table
     *     Allows the user to choose a day and shift type
     *     Transfers control to the selected shift menu
     *
     */
    private static void manageShiftsWeek() {
        while (true) {
            if (shiftService.getWeekStatus() == READY_TO_PUBLISH) {
                if (handlePublishMenu()) {
                    return;
                }
            }

            System.out.println(shiftService.displayWeekAssignments());

            LocalDate date = chooseDay();
            if (date == null) {
                return;
            }

            ShiftType shiftType = chooseShiftType();
            if (shiftType == null) {
                continue;
            }

            Shift selectedShift = shiftService.getShift(date, shiftType);
            manageShift(selectedShift);
        }
    }

    /**
     * Displays and manages the menu for a specific shift.
     *
     * @param shift the selected shift to manage
     */
    private static void manageShift(Shift shift) {
        while (true) {
            printManageShiftMenu(shift);

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> updateShift(shift);
                case 2 -> {
                    if (resetShift(shift)) {
                        return;
                    }
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Displays the update menu for a specific shift.
     *
     * The user can:
     *
     *    Update assignments
     *     Update role requirements
     *    Go back
     *
     *
     * @param shift the selected shift
     */
    private static void updateShift(Shift shift) {
        while (true) {
            System.out.println(shiftService.getShiftDetails(shift));
            printUpdateShiftMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> updateAssignments(shift);
                case 2 -> updateRequirements(shift);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    /**
     * Displays the assignments menu for a specific shift.
     *
     * @param shift the selected shift
     */
    private static void updateAssignments(Shift shift) {
        while (true) {
            System.out.println(shiftService.getShiftDetails(shift));
            printAssignmentsMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> addAssignment(shift);
                case 2 -> replaceEmployee(shift);
                case 3 -> manualChanges(shift);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    /**
     * Assigns an employee to a role in the selected shift.
     *
     * <p>If the regular assignment fails, the method may offer the user
     * a force-assignment approval flow.
     *
     * @param shift the selected shift
     */
    private static void addAssignment(Shift shift) {
        int employeeId;
        System.out.println(shiftService.getAvailableEmployeesForShift(shift));

        System.out.println("Enter employee ID (0 to cancel):");
        employeeId = readIntSafe();
        if (employeeId == 0) {
            return;
        }

        Role role = chooseRole();
        if (role == null) {
            return;
        }

        try {
            shiftService.assignEmployee(shift, role, employeeId);
            System.out.println("Employee assigned successfully.");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            if (employeeId != -1 && shiftService.nobodyToAssign(shift, role)) {
                forceAssign(shift, role, employeeId);
            }
        }
    }

    /**
     * Handles the force-assignment approval flow.
     *
     * @param shift the selected shift
     * @param role the role to assign
     * @param employeeId the employee to assign
     */
    private static void forceAssign(Shift shift, Role role, int employeeId) {
        while (true) {
            System.out.println("Do you want to force assign this employee?");
            System.out.println("1. Yes");
            System.out.println("0. No");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    try {
                        shiftService.forceAssign(shift, role, employeeId);
                        System.out.println("Employee assigned via special approval.");
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                    }
                    return;
                }
                case 0 -> {
                    System.out.println("Operation cancelled.");
                    return;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    /**
     * Replaces one employee in a shift with another employee.
     *
     * @param shift the selected shift
     */
    private static void replaceEmployee(Shift shift) {
        while (true) {
            System.out.println("Enter the ID of the employee currently assigned to the shift (0 to cancel):");
            int currentEmployeeId = readIntSafe();
            if (currentEmployeeId == 0) {
                return;
            }

            System.out.println("Enter the ID of the replacement employee (0 to cancel):");
            int newEmployeeId = readIntSafe();
            if (newEmployeeId == 0) {
                return;
            }

            try {
                shiftService.replaceEmployee(shift, currentEmployeeId, newEmployeeId);
                System.out.println("Replacement successful.");
                return;
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void manualChanges(Shift shift) {
        while (true) {
            System.out.println(shiftService.getShiftDetails(shift));
            printManualChangesMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> manualAssignment(shift);
                case 2 -> manualRemoval(shift);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    private static void manualAssignment(Shift shift) {
        int employeeId;

        System.out.println("Enter employee ID (0 to cancel):");
        employeeId = readIntSafe();
        if (employeeId == 0) {
            return;
        }

        Role role = chooseRole();
        if (role == null) {
            return;
        }

        try {
            shiftService.forceAssign(shift, role, employeeId);
            System.out.println("Employee manually assigned.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void manualRemoval(Shift shift) {
        int employeeId;

        System.out.println("Enter employee ID (0 to cancel):");
        employeeId = readIntSafe();
        if (employeeId == 0) {
            return;
        }

        try {
            shiftService.removeEmployee(shift, employeeId);
            System.out.println("Employee manually removed.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Updates the required number of employees for a given role in a shift.
     *
     * @param shift the selected shift
     */
    private static void updateRequirements(Shift shift) {
        Role role = chooseRole();
        if (role == null) {
            return;
        }

        System.out.println("Enter new required amount:");
        int amount = readIntSafe();

        try {
            shiftService.setRequirement(shift, role, amount);
            System.out.println("Requirement updated.");
        } catch (RuntimeException e) {
            System.out.println("Failed to update requirement: " + e.getMessage());
        }
    }

    /**
     * Removes the selected shift after user confirmation.
     *
     * @param shift the selected shift
     * @return true if the shift was removed, otherwise false
     */
    private static boolean resetShift(Shift shift) {
        while (true) {
            System.out.println("Are you sure you want to reset this shift?");
            System.out.println("1. Yes");
            System.out.println("0. No");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    shiftService.resetShift(shift);
                    System.out.println("Shift reset successfully.");
                    return true;
                }
                case 0 -> {
                    return false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Handles the weekly schedule publication flow.
     *
     * @return true if the schedule was published, otherwise false
     */
    private static boolean handlePublishMenu() {
        System.out.println("All shifts are assigned. Do you want to publish the week schedule?");

        while (true) {
            System.out.println("1. Publish");
            System.out.println("0. Continue managing");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    shiftService.publishWeekSchedule();
                    System.out.println("Week schedule published.");
                    return true;
                }
                case 0 -> {
                    return false;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    /**
     * Lets the user choose a day in the current work week.
     *
     * @return the selected date, or null if the user chose to go back
     */
    private static LocalDate chooseDay() {
        while (true) {
            System.out.println("Manage Shifts Week");
            System.out.println("Enter day number (1-7) or 0 to go back:");
            int dayNumber = readIntSafe();

            if (dayNumber == 0) {
                return null;
            }

            LocalDate weekDay = null;
            try {
                weekDay = getDateOfNextWeekFromDayNumber(dayNumber);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            return weekDay;
        }
    }

    /**
     * Lets the user choose a shift type.
     *
     * @return the selected shift type, or null if the user chose to go back
     */
    private static ShiftType chooseShiftType() {
        while (true) {
            System.out.println("Choose shift type:");
            System.out.println("1. Morning");
            System.out.println("2. Evening");
            System.out.println("0. Back");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    return morning;
                }
                case 2 -> {
                    return evening;
                }
                case 0 -> {
                    return null;
                }
                default -> System.out.println("Invalid shift type.");
            }
        }
    }

    /**
     * Lets the user choose a role from the list of available roles.
     *
     * @return the selected role, or null if the user chose to go back
     */
    private static Role chooseRole() {
        Role[] roles = Role.values();

        while (true) {
            System.out.println("Choose role:");
            for (int i = 0; i < roles.length; i++) {
                System.out.println((i + 1) + ". " + roles[i]);
            }
            System.out.println("0. Back");

            int choice = readIntSafe();

            if (choice == 0) {
                return null;
            }

            try {
                return getRoleFromNumber(choice);
            } catch (Exception e) {
                System.out.println(e.getMessage());;
            }
        }
    }

    /**
     * Updates the constraints submission deadline.
     */
    private static void updateDeadline() {
        System.out.println("Current deadline: " + constraintService.getDeadline());

        while (true) {
            System.out.println("Enter new day (1-7) or 0 to go back:");

            int dayNumber = readIntSafe();

            if (dayNumber == 0) {
                return;
            }

            try {
                DayOfWeek newDay = Parser.getDayFromNumber(dayNumber);
                constraintService.setDeadline(newDay);
                System.out.println("Deadline updated to " + newDay + ".");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Displays the full shifts history.
     */
    private static void showShiftsHistory() {
        System.out.println(shiftService.getShiftHistory());
    }

    /**
     * Safely reads an integer from the user input.
     *
     * @return a valid integer entered by the user
     */
    private static int readIntSafe() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again:");
            }
        }
    }

    /**
     * Prints the main shifts menu.
     */
    private static void printMainMenu() {
        System.out.println("1. Manage Shifts Week");
        System.out.println("3. Manual shift changes (USE ONLY WHEN NECESSARY)");
        System.out.println("3. Get Shifts History");
        System.out.println("4. Update Constraints Deadline");
        System.out.println("0. Back");
    }

    private static void printManageShiftMenu(Shift shift) {
        System.out.println();
        System.out.println("Managing shift: " + shift);
        System.out.println("1. Update Shift");
        System.out.println("2. Reset Shift");
        System.out.println("0. Back");
    }

    private static void printUpdateShiftMenu() {
        System.out.println("1. Update Assignments");
        System.out.println("2. Update Requirements");
        System.out.println("0. Back");
    }

    private static void printAssignmentsMenu() {
        System.out.println("1. Add assignment");
        System.out.println("2. Replace employee");
        System.out.println("0. Back");
    }

    private static void printManualChangesMenu() {
        System.out.println("1. Manual assignment");
        System.out.println("2. Manual removal");
        System.out.println("0. Back");
    }
}