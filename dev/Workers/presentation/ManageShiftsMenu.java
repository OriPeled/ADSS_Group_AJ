package dev.Workers.presentation;

import dev.utils.Parser;
import dev.Workers.service.PreferenceService;
import dev.Workers.service.RequirementService;
import dev.Workers.service.ShiftService;
import dev.Workers.domain.BranchRegistry;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Branch;
import dev.Workers.domain.Objects.Role;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.RoleRegistry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static dev.Workers.domain.Enums.ShiftType.EVENING;
import static dev.Workers.domain.Enums.ShiftType.MORNING;
import static dev.Workers.domain.Enums.WeekStatus.PUBLISHED;
import static dev.Workers.domain.Enums.WeekStatus.READY_TO_PUBLISH;
import static dev.Main.scanner;
import static dev.utils.Parser.*;

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
 *     Updating the preferences' deadline
 *     Displaying shifts history
 *
 *
 * This class belongs to the presentation layer and communicates only
 * with the relevant services.
 */
public class ManageShiftsMenu {
    /** Service responsible for all shift-related operations. */
    private static final ShiftService shiftService = ShiftService.getInstance();
    private static final RequirementService requirementService = RequirementService.getInstance();
    /** Service responsible for employee preferences and deadline management. */
    private static final PreferenceService preferenceService = PreferenceService.getInstance();
    private static final RoleRegistry roleRegistry = RoleRegistry.getInstance();
    private static final BranchRegistry branchRegistry = BranchRegistry.getInstance();

    static void chooseBranch() {
        List<Branch> branches = branchRegistry.getAllBranches();

        if (branches.isEmpty()) {
            System.out.println("No branches available yet. Please load demo data first (main menu option 3).");
            return;
        }

        while (true) {
            System.out.println("Choose branch:");
            for (int i = 0; i < branches.size(); i++) {
                System.out.println((i + 1) + ". " + branches.get(i).getName());
            }
            System.out.println("0. Back");

            int choice = readIntSafe();

            if (choice == 0) {
                return;
            }

            if (choice < 1 || choice > branches.size()) {
                System.out.println("Invalid branch choice. Please try again.");
                continue;
            }

            start(branches.get(choice - 1));
        }
    }

    public static void start(Branch branch) {
        while (true) {
            printMainMenu(branch);

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> manageShiftsWeek(branch);
                case 2 -> manualShiftChanges(branch);
                case 3 -> showShiftsHistory(branch);
                case 4 -> updateDeadline();
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
    private static void manageShiftsWeek(Branch branch) {
        while (true) {
            checkRequirementsSettings(branch);
            checkRequestAnswers(branch);
            if (shiftService.getWeekStatus(branch) == READY_TO_PUBLISH) {
                if (handlePublishMenu(branch)) {
                    return;
                }
            }

            try {
                System.out.println(shiftService.displayWeekAssignments(branch));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            printShiftsWeekMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> accessShift(branch);
                case 2 -> forcePublishWeek(branch);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void checkRequirementsSettings(Branch branch) {
        if (shiftService.isShiftsWeekEmpty(branch)) {
            shiftService.initShiftsWeek(branch);
            requirementService.getDriverReqs(branch);
            requirementService.getStoreKeeperReqs(branch);

            int cashiersAmount;
            while (true) {
                System.out.println("Please enter this week's required shift amount for cashiers:");
                cashiersAmount = readIntSafe();

                try {
                    requirementService.initWeeklyReqs("Cashier", branch, cashiersAmount);
                    System.out.println("Shifts week's cashiers requirements set.");
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            int storekeepersAmount;
            while (true) {
                System.out.println("Please enter this week's required shift amount for storekeepers:");
                storekeepersAmount = readIntSafe();

                try {
                    requirementService.initWeeklyReqs("Storekeeper", branch, storekeepersAmount);
                    System.out.println("Shifts week's storekeepers requirements set.");
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            System.out.println("Shifts week's requirements set.");
        }
    }

    public static void checkRequestAnswers(Branch branch) {
        List<String> notifications = shiftService.popRequestAnswers(branch);

        if (notifications.isEmpty()) {return;}

        System.out.println("\n🔔 --- NEW UPDATES FROM EMPLOYEES ---");
        for (String note : notifications) {
            System.out.println(note);
        }
        System.out.println("--------------------------------------\n");
    }

    /**
     * Handles the weekly schedule publication flow.
     *
     * @return true if the schedule was published, otherwise false
     */
    private static boolean handlePublishMenu(Branch branch) {
        System.out.println("All shifts are assigned. Do you want to publish the week schedule?");

        while (true) {
            System.out.println("1. Publish");
            System.out.println("0. Continue managing");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    try {
                        shiftService.publishNextWeekSchedule(branch);
                        System.out.println("Week schedule published.");
                        return true;
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return false;
                    }
                }
                case 0 -> {
                    return false;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    private static void accessShift(Branch branch) {
        while (true) {
            LocalDate date = chooseDay();
            if (date == null) {
                return;
            }

            ShiftType shiftType = chooseShiftType();
            if (shiftType == null) {
                continue;
            }

            Shift selectedShift = shiftService.getShift(branch, date, shiftType);
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
                case 3 -> extraHours(shift);
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
        if (shiftService.isShiftAssigned(shift)) {
            System.out.println("Shift is fully assigned and managed - no additional assignments needed.");
            return;
        }

        System.out.println(shiftService.getPotentialEmployees(shift));
        
        System.out.println("Enter employee ID (0 to cancel):");
        int empID = readIntSafe();
        if (empID == 0) {
            return;
        }

        Role role = chooseRole();
        if (role == null) {
            return;
        }

        try {
            shiftService.assignEmployee(shift, role, empID);
            System.out.println("Employee assigned successfully.");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            if (empID != -1 && shiftService.needToForceAssign(shift, role, empID)) {
                forceAssign(shift, role, empID);
            }
        }
    }

    /**
     * Handles the force-assignment approval flow.
     *
     * @param shift the selected shift
     * @param role the role to assign
     * @param empID the employee to assign
     */
    private static void forceAssign(Shift shift, Role role, int empID) {
        while (true) {
            System.out.println("Do you want to force assign this employee?");
            System.out.println("1. Yes");
            System.out.println("0. No");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    try {
                        shiftService.sendRequest(shift, role, empID);
                        System.out.println("Assignment request sent to employee.");
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
            System.out.println(shiftService.getPotentialEmployees(shift));
            System.out.println("Enter the ID of the employee currently assigned to the shift (0 to cancel):");
            int curId = readIntSafe();
            if (curId == 0) {
                return;
            }

            System.out.println(shiftService.getPotentialEmployees(shift));
            System.out.println("Enter the ID of the replacement employee (0 to cancel):");
            int newId = readIntSafe();
            if (newId == 0) {
                return;
            }

            if (curId == newId) {
                System.out.println("Error: You entered the same ID twice. Please choose a different replacement.");
                continue;
            }

            try {
                shiftService.replaceEmployee(shift, curId, newId);
                System.out.println("Replacement successful.");
                return;
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
                if (shiftService.needToForceReplace(shift, curId, newId)) {
                    forceReplace(shift, curId, newId);
                    return;
                }
            }
        }
    }

    private static void forceReplace(Shift shift, int curId, int newId) {
        while (true) {
            System.out.println("Do you want to force replace this employee?");
            System.out.println("1. Yes");
            System.out.println("0. No");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    try {
                        shiftService.sendRequest(shift, curId, newId);
                        System.out.println("Replacement request sent to employee.");
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

    private static void extraHours(Shift shift) {
        while (true) {
            System.out.println("===Manage extra hours===");
            System.out.println("Enter employee ID (0 to cancel):");
            int empID = readIntSafe();
            if (empID == 0) {
                return;
            }

            System.out.println("Enter amount of extra hours (0-4)");
            int extraHours = readIntSafe();

            try {
                shiftService.updateExtraHours(shift, empID, extraHours);
                System.out.println("Extra hours updated.");
                return;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

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
     * Resets the selected shift after user confirmation.
     *
     * @param shift the selected shift
     * @return true if the shift was reset, otherwise false
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

    private static void forcePublishWeek(Branch branch) {
        while (true) {
            if (shiftService.getWeekStatus(branch) == PUBLISHED) {
                System.out.println("Week Schedule already published.");
                return;
            }
            if (shiftService.pendingRequestsLeft()) {
                System.out.println("There are still some pending requests.");
            }
            System.out.println("Are you sure you want to force publish the week schedule?");
            System.out.println("1. Yes");
            System.out.println("0. No");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> {
                    try {
                        shiftService.forcePublishNextWeekSchedule(branch);
                        System.out.println("Week Schedule published.");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    return;
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void manualShiftChanges(Branch branch) {
        while (true) {
            LocalDate date = chooseDate();
            if (date == null) {
                return;
            }

            ShiftType shiftType = chooseShiftType();
            if (shiftType == null) {
                continue;
            }

            try {
                Shift selectedShift = shiftService.getExistingShift(branch, date, shiftType);
                manualChanges(selectedShift);
            } catch (Exception e) {
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
                case 3 -> manualExtraHours(shift);
                case 4 -> manualRequirements(shift);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    private static void manualAssignment(Shift shift) {
        System.out.println("===Manual assignment removing===");
        System.out.println("Enter employee ID (0 to cancel):");
        int empID = readIntSafe();
        if (empID == 0) {
            return;
        }

        Role role = chooseRole();
        if (role == null) {
            return;
        }

        try {
            shiftService.manualAssign(shift, role, empID);
            System.out.println("Employee manually assigned.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void manualRemoval(Shift shift) {
        System.out.println("===Manual assignment removing===");
        System.out.println("Enter employee ID (0 to cancel):");
        int empID = readIntSafe();
        if (empID == 0) {
            return;
        }

        try {
            shiftService.removeEmployee(shift, empID);
            System.out.println("Employee manually removed.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void manualExtraHours(Shift shift) {
        System.out.println("===Manual extra hours===");
        System.out.println("Enter employee ID (0 to cancel):");
        int empID = readIntSafe();
        if (empID == 0) {
            return;
        }

        System.out.println("Enter amount of extra hours (0-4)");
        int extraHours = readIntSafe();

        try {
            shiftService.updateExtraHoursManually(shift, empID, extraHours);
            System.out.println("Extra hours updated.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void manualRequirements(Shift shift) {
        System.out.println("===Manual requirements setting===");
        Role role = chooseRole();
        if (role == null) {
            return;
        }

        System.out.println("Enter new required amount:");
        int amount = readIntSafe();

        try {
            shiftService.setRequirementManually(shift, role, amount);
            System.out.println("Requirement updated.");
        } catch (RuntimeException e) {
            System.out.println("Failed to update requirement: " + e.getMessage());
        }
    }

    /**
     * Lets the user choose a day in the current work week.
     *
     * @return the selected date, or null if the user chose to go back
     */
    private static LocalDate chooseDay() {
        while (true) {
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
                continue;
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
                    return MORNING;
                }
                case 2 -> {
                    return EVENING;
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
        List<Role> roles = roleRegistry.getAllRoles();

        while (true) {
            System.out.println("Choose role:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.println((i + 1) + ". " + roles.get(i).getName());
            }
            System.out.println("0. Back");

            int choice = readIntSafe();

            if (choice == 0) {
                return null;
            }

            if (choice < 1 || choice > roles.size()) {
                System.out.println("Invalid role choice. Please try again.");
                continue;
            }

            return roles.get(choice - 1);
        }
    }

    /**
     * Lets the user enter a date for shift accessing.
     *
     * @return the selected date, or null if the user chose to go back
     */
    private static LocalDate chooseDate() {
        LocalDate date;
        while (true) {
            System.out.println("Enter date (format: dd/MM/yyyy) or 0 to go back:");
            try {
                String input = scanner.nextLine();

                if (input.equals("0")) return null;

                date = Parser.stringToDate(input);

                if (date == null) {
                    System.out.println("Invalid date format.");
                    continue;
                }

                break;

            } catch (Exception e) {
                System.out.println("Invalid date. Try again.");
            }
        }
        return date;
    }

    /**
     * Updates the preferences submission deadline.
     */
    private static void updateDeadline() {
        System.out.println("Current deadline: " + preferenceService.getDeadline());

        while (true) {
            System.out.println("Enter new day (1-7) or 0 to go back:");

            int dayNumber = readIntSafe();

            if (dayNumber == 0) {
                return;
            }

            try {
                DayOfWeek newDay = Parser.getDayFromNumber(dayNumber);
                preferenceService.setDeadline(newDay);
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
    private static void showShiftsHistory(Branch branch) {
        System.out.println(shiftService.getShiftHistory(branch));
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

    private static void printMainMenu(Branch branch) {
        System.out.println("=====" + branch + " branch=====");
        System.out.println("1. Manage Shifts Week");
        System.out.println("2. Manual shift changes (USE ONLY WHEN NECESSARY)");
        System.out.println("3. Get Shifts History");
        System.out.println("4. Update Preferences Deadline");
        System.out.println("0. Back");
    }

    private static void printShiftsWeekMenu() {
        System.out.println("1. Access shift");
        System.out.println("2. Force publish week schedule (USE ONLY WHEN NECESSARY)");
        System.out.println("0. Back");
    }

    private static void printManageShiftMenu(Shift shift) {
        System.out.println();
        System.out.println("Managing shift: " + shift);
        System.out.println("1. Update Shift");
        System.out.println("2. Reset Shift (USE ONLY WHEN NECESSARY)");
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
        System.out.println("3. Add extra hours");
        System.out.println("0. Back");
    }

    private static void printManualChangesMenu() {
        System.out.println("1. Manual assignment");
        System.out.println("2. Manual removal");
        System.out.println("3. Update extra hours");
        System.out.println("4. Update requirements");
        System.out.println("0. Back");
    }
}