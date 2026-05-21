package dev.Transportation;

import static dev.Main.scanner;
import static dev.Workers.presentation.Parser.readIntSafe;

public class ManageDriversMenu {
    public static void start() {
        while (true) {
            printMainMenu();
            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> accessDriver();
                    //case 2 -> addDriver();
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static void accessDriver() {
        System.out.println("Enter employee ID (0 to go back):");

        int empId = readIntSafe();

        if (empId == 0) return;

        if (!employeeService.exists(empId)) {
            System.out.println("Employee not found.");
            return;
        }
        manageDriver(empId);
    }

    private static void manageDriver(int empId) {

    }

    private static void addDriver() {

    }

    public static void printMainMenu() {
        System.out.println("Driver Management Menu:");
        System.out.println("1. Manage existing driver");
        System.out.println("2. Add Driver for transportation");
        System.out.println("0. Back");
    }

    public static void printManageEmployeeMenu() {
        System.out.println("1. Driver Details");
        System.out.println("");
        System.out.println("0. Back");
    }

    public static void printDetailsMenu() {
        System.out.println("1. Update Driver's Name");
        System.out.println("2. Update Driver's Salary Type");
        System.out.println("3. Update Driver's Salary Amount");
        System.out.println("4. Update Driver's Job Status");
        System.out.println("5. Update Driver's Constraints");
        System.out.println("0. Back");
    }

}
