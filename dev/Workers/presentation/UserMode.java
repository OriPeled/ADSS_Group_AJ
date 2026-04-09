package dev.Workers.presentation;

import dev.Workers.domain.EmployeeManager;

import static dev.Workers.presentation.Main.scanner;

public class UserMode {
    static EmployeeManager employeeManager = EmployeeManager.getInstance();

    public static void login() {
        System.out.println("User Mode");
        System.out.println("Please Enter ID:");
        int enteredID = scanner.nextInt();
        while (!employeeManager.isEmployee(enteredID)) {
            System.out.println("No Such Employee. Try again or enter 0 to exit.");
            enteredID = scanner.nextInt();
            if (enteredID == 0)
                Main.displayMenu();
        }

        if (!employeeManager.isRegisteredUser(enteredID))
            System.out.println("Please Create Password.");
            employeeManager.setPassword(scanner.nextLine());

        System.out.println("Please Enter Password:");
        String enteredPass = scanner.nextLine();

        while (employeeManager.wrongPassword(enteredPass)) {
            System.out.println("Wrong Password: Try Again or enter 0 to exit.");
            enteredPass = scanner.nextLine();
            Integer input = Integer.valueOf(enteredPass);
            if (input == 0)
                Main.displayMenu();
        }
        start();
    }

    public static void start() {
        System.out.println("Choose Option:");
        System.out.println("1. Update Constraints");
        System.out.println("2. Watch Shifts Schedule");
        System.out.println("3. Logout");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:

            case 2:

            case 3:
                Main.displayMenu();
            default:
                System.out.println("Invalid input.");
        }
    }
}
