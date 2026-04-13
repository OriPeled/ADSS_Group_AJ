package dev.Workers.presentation;

import dev.Workers.domain.ConstraintManager;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.shiftService;
import dev.Workers.domain.ShiftService;

import java.time.LocalDate;

import static dev.Workers.presentation.Main.scanner;

public class ManageShiftsMenu {
    static ShiftService shiftService = ShiftService.getInstance();
    static ConstraintManager constraintManager = ConstraintManager.getInstance();
    static Parser parser;
    static Shift shift;
    //static int id;

    public static void start() {
        System.out.println(shiftService.displayWeekAssignments());
        // sunday morning - FULL
        // sunday evening IN-PROCESS
        // monday morning EMPTY
        // ...
        // saturday evening FULL
        System.out.println("Manage Shifts Week");
        System.out.println("1. Enter Day (1-7)");
        int dayNumber = scanner.nextInt();
        LocalDate date = Parser.dayNumberToDate(dayNumber);
        System.out.println("2. Enter Shift (1 for morning, 2 for evening)");
        String type = scanner.nextLine();
        shiftType shiftT = shiftType.valueOf(type);
        shift = shiftService.getShift(date, shiftT);
        manageShift();

        /*int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                accessShift();
            case 2:
                addShift();
            case 3:
                AdminMode.start();
            default:
                System.out.println("Invalid choice.");
        }*/
    }
    

    private static void manageShift() {
        System.out.println("1. Update Shift");
        System.out.println("2. Remove Shift");
        System.out.println("3. Back");
        System.out.println("");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                updateShift();
            case 2:
                removeShift();
            case 3:
                start();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void updateShift() {
        System.out.println(shiftService.getShiftDetails(shift)); // (how many roles/employees left to assign)

        // sunday morning
        // cashier: ido, adi, ali (3 assigned, 4 left to assign)
        // storekeeper: muhamad, mesi (2 assigned, 2 left to assign)

        //shiftService.assignEmployeeToShift();
        System.out.println("1. Update Assignments");
        System.out.println("2. Update Requirements");
        System.out.println("3. Update Constraints Deadline");
        System.out.println("4. Back");
        int choice = scanner.nextInt();
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
                start();
                break;
            default:
                System.out.println("Invalid input.");
                updateShift();
        }
    }

    public static void updateAssignments() {
        System.out.println("1. Add assignment");
        System.out.println("2. Make a replacement");
        System.out.println("3. Back");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                addAssignment();
                break;
            case 2:
                replace();
                break;
            case 3:
                updateAssignments();
                break;
            default:
                System.out.println("Invalid input.");
        }
    }

    private static void addAssignment() {
        System.out.println(shiftService.getUnassignedValid(shift));

        System.out.println("Enter Employee ID");
        int id = scanner.nextInt();
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int roleNumber = Integer.parseInt(scanner.nextLine());
        Role role = Role.values()[roleNumber - 1];
        shiftService.assignEmployee(shift, role, id);
        System.out.println("Employee assigned.");
        updateAssignments();
    }

    private static void replace() {
        System.out.println("Enter the ID of the already assigned employee");
        int currentEmployeeId = scanner.nextInt();
        System.out.println("Enter the ID of the employee to replace him");
        int newEmployeeId = scanner.nextInt();
        shiftService.replaceEmployee(shift, currentEmployeeId, newEmployeeId);
        System.out.println("Replacement successful.");
        updateAssignments();
    }

    private static void updateRequirements() {
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int roleNumber = Integer.parseInt(scanner.nextLine());
        Role role = Role.values()[roleNumber - 1];
        System.out.println("Enter new amount.");
        int amount = scanner.nextInt();
        shiftService.setRequirements(shift, role, amount);
        System.out.println("Amount updated.");
        updateShift();
    }

    private static void updateDeadline() {
        System.out.print("Enter new date (dd/mm/yyyy) or 0 to go back:");
        int input = scanner.nextInt();
        if (input == 0)
            start();
        String dateString = String.valueOf(input);
        LocalDate date = Parser.stringToDate(dateString);

        constraintManager.setDeadline(date);
    }

    /*public static void assignEmployee() {
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int choice = scanner.nextInt();
        shiftService.assignEmployee(shift, choice, id);
    }

    private static void removeShift() {
        System.out.println("Are you sure you want to remove " + shift.getType() + " (" + shift.getShiftDate() + ")?" +
                           "If yes - enter 1, else 0.");

        int choice = scanner.nextInt();
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
        int input = scanner.nextInt();
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
