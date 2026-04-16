package dev.Workers.presentation;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.Status;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Employee;

import java.time.DayOfWeek;

import static dev.Workers.domain.Enums.Status.*;
import static dev.Workers.domain.Enums.ShiftType.*;
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

            String input = scanner.nextLine();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    updateConstraints();
                    break;
                case 2:
                    watchShifts();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private static void watchShifts() {
        while (true) {
            String shifts = shiftService.getEmployeeShifts(employee.getId());
            System.out.println(shifts);

            System.out.println("Enter 0 to return.");
            String input = scanner.nextLine();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                continue;
            }

            switch (choice) {
                case 1:
                    return;
                case 2:
                    System.out.println("Invalid input.");
            }
        }
    }

    public static void updateConstraints() {
        while (true) {
            System.out.println(constraintService.display(employee.getId()));
            // sunday - morning(yes), evening(yes)
            System.out.println("Choose a shift constraint to change or enter 0 to exit.");

            System.out.println("Choose 1-7 for day");
            String dayInput = scanner.nextLine();
            int dayNumber;
            try {
                dayNumber = Integer.parseInt(dayInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                continue;
            }

            if (dayNumber == 0) return;

            DayOfWeek day;
            try {
                day = ConstraintService.getDayFromNumber(dayNumber);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            System.out.println("Choose 1 for morning, 2 for evening, 3 for any, 4 for rest");
            String shiftInput = scanner.nextLine();
            int shiftChoice;
            try {
                shiftChoice = Integer.parseInt(shiftInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                continue;
            }

            if (shiftChoice == 0) return;

            ShiftType shiftType;
            switch (shiftChoice) {
                case 1:
                    shiftType = morning;
                    break;
                case 2:
                    shiftType = evening;
                    break;
                case 3:
                    shiftType = any;
                    break;
                case 4:
                    shiftType = rest;
                    break;
                default:
                    System.out.println("Invalid input.");
                    continue;
            }
            constraintService.update(employee.getId(), day, shiftType);
        }
    }
}
