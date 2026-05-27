package dev.Workers.presentation;

//import dev.Workers.domain.Objects.HR_Admin;

import static dev.Main.scanner;
import static dev.Workers.presentation.Parser.readIntSafe;

/**
 * Handles Admin (HR Manager) mode UI flow.
 *
 * Responsible for:
 * - Admin login / password creation
 * - Authentication loop
 * - Navigation to employee/shift management menus
 */
public class adminMode {
    private static final String HR_PASSWORD = "8888";
    /**
     * Handles admin login process.
     * If first time login, creates password.
     * Then validates password before entering system.
     */
    public static void login() {
        System.out.println("HR Manager Mode");
        System.out.println("Please Enter Password (or 0 to cancel):");
        while (true) {
            String enteredPass = scanner.nextLine();
            if (enteredPass.equals("0")) {
                return;
            }
            if (enteredPass.equals(HR_PASSWORD)) {
                start();
                return;
            }
            System.out.println("Wrong Password. Try Again or enter 0 to cancel:");
        }
    }

    /**
     * Displays admin management menu and routes user actions.
     */
    public static void start() {
        while (true) {
            System.out.println("Choose to Manage:");
            System.out.println("1. Employees");
            System.out.println("2. Shifts");
            System.out.println("0. Logout");

            int choice = readIntSafe();
            switch (choice) {
                case 1:
                    ManageEmployeesMenu.start();
                    break;
                case 2:
                    ManageShiftsMenu.start();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }
}
