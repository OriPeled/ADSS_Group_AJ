package dev.Workers.presentation;

import dev.Workers.domain.Shift;
import dev.Workers.domain.ShiftManager;
import dev.Workers.domain.shiftType;

import java.time.LocalDate;

import static dev.Workers.presentation.Main.displayMenu;
import static dev.Workers.presentation.Main.scanner;

public class ShiftsMenu {
    static ShiftManager shiftManager = ShiftManager.getInstance();

    static Shift shift;

    public static void shiftsMenu() {
        System.out.println("Shifts");
        System.out.println("1. Manage existing Shift");
        System.out.println("2. Add Shift");
        System.out.println("3. Back");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                manageShiftMenu();
            case 2:
                addShiftMenu();
            case 3:
                displayMenu();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void manageShiftMenu() {
        System.out.print("Enter date (dd/mm/yyyy):");
        String dateString = scanner.nextLine();
        LocalDate date = Parser.stringToDate(dateString);

        System.out.print("Enter type (morning/evening):");
        String shiftTypeString = scanner.nextLine();

        shift = shiftManager.getShift(date, shiftTypeString);
        System.out.print("Shift chosen.");

        System.out.println("1. Update Shift");
        System.out.println("2. Remove Shift");
        System.out.println("3. Back");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                updateShift();
            case 2:
                removeShift();
            case 3:
                shiftsMenu();
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void updateShift() {
        // TODO
    }

    private static void removeShift() {
        System.out.println("Are you sure you want to remove " + shift.getShift() + " (" + shift.getShiftDate() + ")? If yes - enter 1, else 0.");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                shiftManager.removeShift(shift);
                System.out.println("Shift removed.");
                manageShiftMenu();
            case 2:
                manageShiftMenu();
        }
    }

    private static void addShiftMenu() {
        shiftManager.addShift(shift.getShift(), shift.getShiftDate());
        System.out.println("Shift added.");
        shiftsMenu();
    }
}
