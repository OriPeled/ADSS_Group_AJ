package dev.Workers.presentation;

import dev.Workers.Service.AccessService;
import dev.Workers.Service.ConstraintManager;
import dev.Workers.Service.EmployeeManager;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Employee;

import java.time.DayOfWeek;

import static dev.Workers.domain.Enums.shiftType.*;
import static dev.Workers.presentation.Main.scanner;

public class UserMode {
    static EmployeeManager employeeManager = EmployeeManager.getInstance();
    static ConstraintManager constraintManager = ConstraintManager.getInstance();
    static AccessService accessService = AccessService.getInstance();

    static Employee employee;

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

        if (!accessService.isRegisteredUser(enteredID)) {
            System.out.println("Please Create Password.");
            accessService.Register(enteredID,scanner.nextLine());
        }
        System.out.println("Please Enter Password:");
        String enteredPass = scanner.nextLine();

        while (accessService.getAccess(enteredID).getPassword() != enteredPass) {
            System.out.println("Wrong Password: Try Again or enter 0 to exit.");
            enteredPass = scanner.nextLine();
            java.lang.Integer input = java.lang.Integer.valueOf(enteredPass);
            if (input == 0)
                Main.displayMenu();
        }
        employee = employeeManager.getById(enteredID);
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
                updateConstraints();
                break;
            case 2:

            case 3:
                Main.displayMenu();
            default:
                System.out.println("Invalid input.");
        }
    }

    public static void updateConstraints() {
        constraintManager.display(employee.getId());
        // sunday - morning(yes), evening(yes)
        System.out.println("Choose a shift constraint to change");
        System.out.println("Choose 1-7 for day");
        int dayNumber = scanner.nextInt();
        DayOfWeek day = ConstraintManager.getDayFromNumber(dayNumber);
        //System.out.println("Choose 1 for morning and 2 for evening");
        System.out.println("Choose 1 for morning, 2 for evening, 3 for rest");
        int choice = scanner.nextInt();
        shiftType shiftType;
        switch (choice) {
            case 1:
                shiftType = morning;
                break;
            case 2:
                shiftType = evening;
                break;
            case 3:
                shiftType = notWorking;
                break;
            default:
                shiftType = wholeDay;
                break;
        }
        constraintManager.update(employee.getId(), day, shiftType);
    }
}
