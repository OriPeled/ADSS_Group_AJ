package dev.Workers.presentation;

import dev.Workers.Service.ConstraintService;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Enums.WeekStatus;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.Service.ShiftService;

import java.time.LocalDate;

import static dev.Workers.domain.Enums.ShiftType.evening;
import static dev.Workers.domain.Enums.ShiftType.morning;
import static dev.Workers.presentation.Main.scanner;

public class ManageShiftsMenu {
    static ShiftService shiftService = ShiftService.getInstance();
    static ConstraintService constraintService = ConstraintService.getInstance();
    static Shift shift;
    //static int id;

    public static void start() {
        System.out.println(shiftService.displayWeekAssignments());
        // sunday morning - FULL
        // sunday evening IN-PROCESS
        // monday morning EMPTY
        // ...
        // saturday evening FULL
        while (true) {
            System.out.println("1. Manage Shifts Week");
            System.out.println("2. Get Shifts History");
            System.out.println("3. Back");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    checkShiftsWeek();
                    break;
                case 2:
                    getShiftsHistory();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void checkShiftsWeek() {
        WeekStatus status = shiftService.getWeekStatus();

        switch (status) {
            case INCOMPLETE:
                manageShiftsWeek();
                break;
            case PUBLISHED:
                break;
            case READY_TO_PUBLISH:
                handlePublishMenu();
                break;
        }
    }

    private static void handlePublishMenu() {
        System.out.println("All shifts assigned. Do you wish to public the week schedule?");
        System.out.println("Enter 1 to publish or 0 to continue managing the schedule.");
        while (true) {
            String input = scanner.nextLine();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 0:
                    manageShiftsWeek();
                    break;
                case 1:
                    shiftService.publishWeekSchedule();
                    System.out.println("Shifts schedule published.");
                    manageShiftsWeek();
            }
        }
    }

    private static void manageShiftsWeek() {
        System.out.println(shiftService.displayWeekAssignments());
        System.out.println("Manage Shifts Week");
        //if (shiftService.allWeekAssigned()) { // assignments
        //    System.out.println("All week shifts are assigned. Do you wish to mark the week schedule as finished?");
            // shiftService/assignments.markWeekFinished();    // (constraints, deadline reset + publicNextWeek logic)
        //}

        System.out.println("1. Enter Day (1-7)");
        int dayNumber = Integer.parseInt(scanner.nextLine());
        LocalDate date = Parser.dayNumberToDate(dayNumber);
        System.out.println("Enter Shift (1 for morning, 2 for evening)");
        int typeNumber = Integer.parseInt(scanner.nextLine());
        ShiftType shiftT;
        switch (typeNumber) {
            case 1 -> shiftT = morning;
            case 2 -> shiftT = evening;
            default -> {
                System.out.println("Invalid shift type.");
                return;
            }
        }
         shift = shiftService.getShift(date, shiftT);
        manageShift();
    }

    private static void manageShift() {
        while (true) {
            System.out.println();
            System.out.println("1. Update Shift");
            System.out.println("2. Remove Shift");
            System.out.println("3. Back");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    updateShift();
                    break;
                case 2:
                    removeShift();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void removeShift() {
        while (true) {
            System.out.println("Are you sure you want to remove this shift? If yes, enter 1, else enter 0.");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    shiftService.removeShift(shift);
                    System.out.println("Shift successfully removed.");
                    return;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void updateShift() {
        while (true) {
            System.out.println(shiftService.getShiftDetails(shift));
            System.out.println("1. Update Assignments");
            System.out.println("2. Update Requirements");
            System.out.println("3. Update Constraints Deadline");
            System.out.println("4. Back");

            String input = scanner.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    updateAssignments();
                    break;
                case 2:
                    updateRequirements();
                    break;
                case 3:
                    updateDeadline();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }


    public static void updateAssignments() {
        while (true) {
            System.out.println("1. Add assignment");
            System.out.println("2. Make a replacement");
            System.out.println("3. Back");

            String input = scanner.nextLine();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    addAssignment();
                    break;
                case 2:
                    replace();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private static void addAssignment() {
        System.out.println(shiftService.getAvailableEmployeesForShift(shift));
        System.out.println("Enter Employee ID");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int roleNumber = Integer.parseInt(scanner.nextLine());
        Role role = Role.values()[roleNumber - 1];
        shiftService.assignEmployee(shift, role, id);
        System.out.println("Employee assigned.");
    }

    private static void replace() {
        System.out.println("Enter the ID of the already assigned employee");
        int currentEmployeeId = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter the ID of the employee to replace him");
        int newEmployeeId = Integer.parseInt(scanner.nextLine());
        shiftService.replaceEmployee(shift, currentEmployeeId, newEmployeeId);
        System.out.println("Replacement successful.");
    }

    private static void updateRequirements() {
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int roleNumber = Integer.parseInt(scanner.nextLine());
        Role role = Role.values()[roleNumber - 1];
        System.out.println("Enter new amount.");
        int amount = Integer.parseInt(scanner.nextLine());
        shiftService.setRequirement(shift, role, amount);
        System.out.println("Amount updated.");
    }

    private static void updateDeadline() {
        System.out.print("Enter new date (dd/mm/yyyy) or 0 to go back:");
        int input = Integer.parseInt(scanner.nextLine());
        if (input == 0)
            return;
        String dateString = String.valueOf(input);
        LocalDate date = Parser.stringToDate(dateString);

        constraintService.setDeadline(date);
    }

    public static void getShiftsHistory() {
        System.out.println(shiftService.getShiftHistory());
    }

    /*public static void assignEmployee() {
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int choice = Integer.parseInt(scanner.nextLine());
        shiftService.assignEmployee(shift, choice, id);
    }

    private static void removeShift() {
        System.out.println("Are you sure you want to remove " + shift.getType() + " (" + shift.getShiftDate() + ")?" +
                           "If yes - enter 1, else 0.");

        int choice = Integer.parseInt(scanner.nextLine());
        switch (choice) {
            case 1:
                shiftService.removeShift(shift);
                System.out.println("Shift removed.");
                manageShift();
            case 2:
                manageShift();
        }
    }

    private static void addShift() {
        System.out.print("Enter date (dd/mm/yyyy) or 0 to go back:");
        int input = Integer.parseInt(scanner.nextLine());
        if (input == 0)
            start();
        String dateString = String.valueOf(input);
        LocalDate date = Parser.stringToDate(dateString);

        System.out.print("Enter type (morning/evening):");
        shiftType type = shiftType.valueOf(scanner.nextLine());

        shiftService.addShift(date, type);
        shift = shiftService.getShift(date, type);
        System.out.println("Shift added.");
        updateShift();
    }*/
}
