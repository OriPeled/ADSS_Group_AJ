package dev.Workers.presentation;

//import dev.Workers.domain.Objects.HR_Admin;

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
                return;      // back to Main's loop
            }
            if (enteredPass.equals(HR_PASSWORD)) {
                start();                        // go to admin menu
                return;                         // after start() finishes, leave login
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
            System.out.println("3. Logout");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    ManageEmployeesMenu.start();
                    break;
                case 2:
                    ManageShiftsMenu.start();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }
}
