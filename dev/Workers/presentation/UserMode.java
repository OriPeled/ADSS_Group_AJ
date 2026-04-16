package dev.Workers.presentation;

import dev.Workers.Service.AccessService;
import dev.Workers.Service.ConstraintService;
import dev.Workers.Service.EmployeeService;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Employee;

import java.time.DayOfWeek;

import static dev.Workers.domain.Enums.shiftType.*;
import static dev.Workers.presentation.Main.scanner;

public class UserMode {
    static EmployeeService employeeService = EmployeeService.getInstance();
    static ConstraintService constraintService = ConstraintService.getInstance();
    static AccessService accessService = AccessService.getInstance();

    static Employee employee;

    public static void login() {
        System.out.println("User Mode");
        int enteredID;
        while(true) {
            System.out.println("Please Enter ID or 0 to return:");
            String input = scanner.nextLine();
            try {
                enteredID = Integer.parseInt(input);
                if (enteredID == 0) Main.displayMenu(); // return
                if (employeeService.isEmployee(enteredID)) break; // valid ID
                else {System.out.println("No Such Employee. Try again or enter 0 to exit.");}
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }

        if (!accessService.isRegisteredUser(enteredID)) {
            System.out.println("Please Create Password.");
            String enteredPassword = scanner.nextLine();
            accessService.Register(enteredID,enteredPassword);
        }

        System.out.println("Please Enter Password:");
        String enteredPass = scanner.nextLine();
        while (!accessService.getAccess(enteredID).getPassword().equals(enteredPass)) {
            System.out.println("Wrong Password: Try Again or enter 0 to exit.");
            enteredPass = scanner.nextLine();
            java.lang.Integer input = java.lang.Integer.valueOf(enteredPass);
            if (input == 0)
                Main.displayMenu();
        }
        employee = employeeService.getEmployee(enteredID);
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
       System.out.println(constraintService.display(employee.getId()));
        // sunday - morning(yes), evening(yes)
        System.out.println("Choose a shift constraint to change or enter 0 to exit.");
        System.out.println("Choose 1-7 for day");
        int dayNumber = scanner.nextInt();
        if (dayNumber == 0)
            start();
        DayOfWeek day = ConstraintService.getDayFromNumber(dayNumber);
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
                shiftType = any;
                break;
        }
        constraintService.update(employee.getId(), day, shiftType);
        updateConstraints();
    }
}
