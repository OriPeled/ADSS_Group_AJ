package dev.Workers.presentation;

import dev.Workers.domain.ShiftAssignments;
import dev.Workers.domain.ShiftManager;

import java.time.LocalDate;

import static dev.Workers.presentation.Main.scanner;

public class ManageShiftsMenu {
    static ShiftManager shiftManager = ShiftManager.getInstance();
    static ShiftAssignments shift;

    public static void start() {
        // shiftManager.displayShiftsWeek()
        System.out.println("Manage Shifts Week");
        System.out.println("1. Enter Day (1-7)");
        System.out.println("2. Enter Shift (1 for morning, 2 for evening)");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                accessShift();
            case 2:
                addShift();
            case 3:
                AdminMode.start();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void accessShift() {
        System.out.print("Enter date (dd/mm/yyyy) or 0 to go back:");
        int input = scanner.nextInt();
        if (input == 0)
            start();
        String dateString = String.valueOf(input);
        LocalDate date = Parser.stringToDate(dateString);

        System.out.print("Enter type (morning/evening):");
        String shiftTypeString = scanner.nextLine();

        shift = shiftManager.getShift(date, shiftTypeString);
        System.out.print("Shift chosen.");
        manageShift();
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
        shift.toString();
        shiftManager.displayAssignmentStatus(shift); // (how many roles/employees left to assign)

        // sunday morning
        // cashier: ido, adi, ali (3 assigned, 4 left to assign)
        // storekeeper: muhamad, mesi (2 assigned, 2 left to assign)

        //shiftManager.assignEmployeeToShift();
        System.out.println("1. Update Assignments");
        System.out.println("2. Update Requirements");
        System.out.println("3. Update Constraints Deadline");
        System.out.println("4. Back");
        // TODO
    }

    private static void removeShift() {
        System.out.println("Are you sure you want to remove " + shift.getShift() + " (" + shift.getShiftDate() + ")?" +
                           "If yes - enter 1, else 0.");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                shiftManager.removeShift(shift);
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
        String shiftTypeString = scanner.nextLine();

        shiftManager.addShift(date, shiftTypeString);
        shift = shiftManager.getShift(date, shiftTypeString);
        System.out.println("Shift added.");
        updateShift();
    }
}
