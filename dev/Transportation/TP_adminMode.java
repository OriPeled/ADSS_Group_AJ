package dev.Transportation;

import dev.Workers.presentation.ManageEmployeesMenu;
import dev.Workers.presentation.ManageShiftsMenu;

import static dev.Main.scanner;
import static dev.Workers.presentation.Parser.readIntSafe;

public class TP_adminMode {
    private static final String TP_PASSWORD = "9999";
    public static void login() {
        System.out.println("Transportation Manager Mode");
        System.out.println("Please Enter Password (or 0 to cancel):");
        while (true) {
            String enteredPass = scanner.nextLine();
            if (enteredPass.equals("0")) {
                return;
            }
            if (enteredPass.equals(TP_PASSWORD)) {
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
            System.out.println("1. Drivers");
            System.out.println("2. Transportations");
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
