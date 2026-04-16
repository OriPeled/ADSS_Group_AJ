package dev.Workers.presentation;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.Status;
import dev.Workers.domain.Enums.shiftType;
import dev.Workers.domain.Objects.Employee;

import java.time.DayOfWeek;

import static dev.Workers.domain.Enums.Status.*;
import static dev.Workers.domain.Enums.shiftType.*;
import static dev.Workers.presentation.Main.scanner;

public class UserMode {
    static EmployeeService employeeService = EmployeeService.getInstance();
    static ConstraintService constraintService = ConstraintService.getInstance();
    static AccessService accessService = AccessService.getInstance();
    static ShiftService shiftService = ShiftService.getInstance();

    static Employee employee;

    public static void login() {
        System.out.println("User Mode");
        while (true) {
            System.out.println("Please enter ID or 0 to cancel:");
            String idInput = scanner.nextLine();
            int enteredID;
            try {
                enteredID = Integer.parseInt(idInput);
                if (enteredID == 0) return;
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID format.");
                continue;
            }

            System.out.println("Please enter password or 0 to cancel:");
            String enteredPassword = scanner.nextLine();
            if (enteredPassword.equals("0")) return;

            Status loginResponse = accessService.login(enteredID, enteredPassword);

            if (loginResponse == success) {
                employee = employeeService.getEmployee(enteredID);
                start();
                return;
            }
            else if (loginResponse == wrongPassword) {
                System.out.println("Wrong password.");
            }
            else if (loginResponse == notRegistered) {
                handleRegistration(enteredID);
            }
            else if (loginResponse == notInSystem) {
                System.out.println("No such employee.");
            }
        }
    }

    private static void handleRegistration(int id) {
        System.out.println("User not registered. Please create password (at least 4 characters).");
        while (true) {
            System.out.println("Please enter new password or 0 to cancel:");
            String newPass = scanner.nextLine();
            if (newPass.equals("0")) return;

            Status registerResponse = accessService.Register(id, newPass);

            if (registerResponse == invalidPassword) {
                System.out.println("Invalid Password. Please enter at least 4 characters.");
            } else {
                System.out.println("Successfully registered.");
                break;
            }
        }
    }

    public static void start() {
        while (true) {
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
                    WatchShifts();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private static void WatchShifts() {
        String shifts = shiftService.getEmployeeShifts(employee.getId());
        System.out.println(shifts);
    }

    public static void updateConstraints() {
       System.out.println(constraintService.display(employee.getId()));
        // sunday - morning(yes), evening(yes)
        System.out.println("Choose a shift constraint to change or enter 0 to exit.");
        System.out.println("Choose 1-7 for day");
        int dayNumber = scanner.nextInt();
        if (dayNumber == 0)
            return;
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
