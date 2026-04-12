package dev.Workers.presentation;

import dev.Workers.domain.Objects.Shift;
import dev.Workers.domain.ShiftService;

import static dev.Workers.presentation.Main.scanner;

public class ShiftsMenu {

    private static ShiftService shiftService = ShiftService.getInstance();
    private static Shift currentShift;

    public static void start() {
        printMainMenu();

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                accessShiftMenu();
                break;
            case 2:
                ShiftsActions.addShift();
                start();
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid choice.");
                start();
        }
    }

    // ================= MAIN MENU =================
    private static void printMainMenu() {
        System.out.println("\n=== SHIFTS MENU ===");
        System.out.println("1. Access Shift");
        System.out.println("2. Add Shift");
        System.out.println("3. Back");
    }

    // ================= SHIFT MENU =================
    private static void printShiftMenu() {
        System.out.println("\n=== SHIFT MENU ===");
        System.out.println("1. View Details");
        System.out.println("2. Update Shift");
        System.out.println("3. Remove Shift");
        System.out.println("4. Back");
    }

    // ================= UPDATE MENU =================
    private static void printUpdateMenu() {
        System.out.println("\n=== UPDATE MENU ===");
        System.out.println("1. Assign Employee");
        System.out.println("2. Remove Employee");
        System.out.println("3. Update Requirements");
        System.out.println("4. Back");
    }

    // ================= FLOW =================
    private static void accessShiftMenu() {
        currentShift = ShiftsActions.chooseShift();

        if (currentShift == null) {
            start();
            return;
        }

        manageShiftMenu();
    }

    private static void manageShiftMenu() {
        printShiftMenu();

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                ShiftsActions.printShiftDetails(currentShift);
                manageShiftMenu();
                break;
            case 2:
                updateMenu();
                break;
            case 3:
                ShiftsActions.removeShift(currentShift);
                start();
                break;
            case 4:
                start();
                break;
            default:
                System.out.println("Invalid choice.");
                manageShiftMenu();
        }
    }

    private static void updateMenu() {
        printUpdateMenu();

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                ShiftsActions.assignEmployee(currentShift);
                break;
            case 2:
                ShiftsActions.removeEmployee(currentShift);
                break;
            case 3:
                ShiftsActions.updateRequirements(currentShift);
                break;
            case 4:
                manageShiftMenu();
                return;
            default:
                System.out.println("Invalid choice.");
        }

        updateMenu();
    }
}