package dev.Workers.presentation;

import static dev.Workers.presentation.Main.scanner;

/**
 * Handles Admin (HR Manager) mode UI flow.
 *
 * Responsible for:
 * - Admin login / password creation
 * - Authentication loop
 * - Navigation to employee/shift management menus
 */
public class AdminMode {
    private static String password;
    private static boolean isRegistered = false;

    /**
     * Handles admin login process.
     * If first time login, creates password.
     * Then validates password before entering system.
     */
    public static void login() {
        System.out.println("HR Manager Mode");
        if (!isRegistered) {
            // First-time setup: create password
            System.out.println("Please Create Password:");
            password = scanner.nextLine();
            System.out.println("Password Created.");

            isRegistered = true;
        }
        // Password authentication
        System.out.println("Please Enter Password:");
        String enteredPass = scanner.nextLine();
        while (enteredPass != password) {
            System.out.println("Wrong Password: Try Again or enter 0 to exit.");
            enteredPass = scanner.nextLine();
            Integer input = Integer.valueOf(enteredPass);
            if (input == 0)
                Main.displayMenu();
        }
        start();
    }
    /**
     * Displays admin management menu and routes user actions.
     */
    public static void start() {
        System.out.println("Choose to Manage:");
        System.out.println("1. Employees");
        System.out.println("2. Shifts");
        System.out.println("3. Logout");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                ManageEmployeesMenu.start();
            case 2:
                ManageShiftsMenu.start();
            case 3:
                Main.displayMenu();
            default:
                System.out.println("Invalid input.");
        }
    }
}
