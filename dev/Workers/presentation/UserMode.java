package dev.Workers.presentation;

import dev.Workers.Service.*;
import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Enums.ShiftType;

import java.time.DayOfWeek;

import static dev.Workers.domain.Enums.UserResponse.*;
import static dev.Workers.domain.Enums.ShiftType.*;
import static dev.Workers.presentation.Main.scanner;
import static dev.Workers.presentation.Parser.readIntSafe;

public class UserMode {
    static ConstraintService constraintService = ConstraintService.getInstance();
    static AccessService accessService = AccessService.getInstance();
    static ShiftService shiftService = ShiftService.getInstance();
    static EmployeeService employeeService = EmployeeService.getInstance();

    static int employeeId;

    public static void login() {
        System.out.println("User Mode");

        while (true) {
            System.out.println("Please enter ID or 0 to cancel:");
            int enteredID = readIntSafe();
            if (enteredID == 0) return;

            System.out.println("Please enter password or 0 to cancel:");
            String enteredPassword = scanner.nextLine();
            if (enteredPassword.equals("0")) return;

            try {
                UserResponse loginResponse = accessService.login(enteredID, enteredPassword);
                if (loginResponse == success) {
                    employeeId = enteredID;
                    start();
                    return;
                }
                else if (loginResponse == notRegistered) {
                    handleRegistration(enteredID);
                    return;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void handleRegistration(int id) {
        System.out.println("User not registered. Please create password (at least 4 characters).");

        while (true) {
            System.out.println("Please enter new password or 0 to cancel:");
            String newPass = scanner.nextLine();
            if (newPass.equals("0")) return;
            try {
                accessService.Register(id, newPass);
                System.out.println("User with ID: " + id + " is now registered and logged in.");
                start();
                return;
            }
             catch (Exception e) {
                System.out.println(e.getMessage());
             }
        }
    }

    public static void start() {
        while (true) {
            printMainMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1:
                    updateConstraints();
                    break;
                case 2:
                    currentWeekShifts();
                    break;
                case 3:
                    nextWeekShifts();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private static void currentWeekShifts() {
        while (true) {
            printCurrentWeekMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1:
                    watchMyCurrentWeek();
                    break;
                case 2:
                    watchCurrentFullSchedule();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private static void watchMyCurrentWeek() {
        String empShifts = shiftService.getEmployeeShifts(employeeId);
        System.out.println(empShifts);
    }

    private static void watchCurrentFullSchedule() {
        System.out.println(shiftService.displayCurrentWeek());
    }

    private static void nextWeekShifts() {
        while (true) {
            printnextWeekMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1:
                    watchMyNextWeek();
                    break;
                case 2:
                    watchNextWeekSchedule();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    // can be seen only after admin publishes schedule
    private static void watchMyNextWeek() {
        String empShifts = shiftService.getNextWeekEmployeeShifts(employeeId);
        System.out.println(empShifts);
    }

    private static void watchNextWeekSchedule() {
        System.out.println(shiftService.displayWeekAssignments());
    }

    public static void updateConstraints() {
        if (!constraintService.isOnTime()) {
            System.out.println("Deadline for updating has passed.");
            return;
        }
        System.out.println(constraintService.display(employeeId));
        while (true) {
            System.out.println("Choose a shift constraint to change or enter 0 to cancel.");
            System.out.println("Choose 1-7 for day");
            int dayNumber = readIntSafe();

            if (dayNumber == 0) return;

            DayOfWeek day;
            try {
                day = Parser.getDayFromNumber(dayNumber);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input. Day must be between 1 and 7.");
                continue;
            }

            System.out.println("Choose 1 for morning, 2 for evening, 3 for any, 4 for rest, 0 to cancel");

            int shiftChoice = readIntSafe();
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
                case 0:
                    return;
                default:
                    System.out.println("Invalid input.");
                    continue;
            }
            try {
                constraintService.update(employeeId, day, shiftType);
                System.out.println("Constraint updated successfully.");
                System.out.println(constraintService.display(employeeId));
            }
            catch (RuntimeException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }

    public static void printMainMenu() {
        System.out.println("Choose Option:");
        System.out.println("1. Update Constraints");
        System.out.println("2. Watch Current Week's Shifts");
        System.out.println("3. Watch Next Week's Shifts");
        System.out.println("0. Logout");
    }

    public static void printCurrentWeekMenu() {
        System.out.println("===Current Week Shifts===");
        System.out.println("1. Watch My Shifts");
        System.out.println("2. Watch Full Schedule");
        System.out.println("0. Back");
    }

    public static void printnextWeekMenu() {
        System.out.println("===Next Week Shifts===");
        System.out.println("1. Watch My Shifts");
        System.out.println("2. Watch Full Schedule");
        System.out.println("0. Back");
    }
}
