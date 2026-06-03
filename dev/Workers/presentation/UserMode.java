package dev.Workers.presentation;

import dev.Workers.service.ConstraintService;
import dev.Workers.domain.Enums.UserResponse;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Branch;

import java.time.DayOfWeek;

import static dev.Workers.domain.Enums.UserResponse.*;
import static dev.Workers.domain.Enums.ShiftType.*;
import static dev.Main.scanner;
import static dev.Workers.presentation.Parser.readIntSafe;

public class UserMode {
    static dev.Workers.service.ConstraintService constraintService = ConstraintService.getInstance();
    static dev.Workers.service.AccessService accessService = dev.Workers.service.AccessService.getInstance();
    static dev.Workers.service.ShiftService shiftService = dev.Workers.service.ShiftService.getInstance();
    static dev.Workers.service.EmployeeService employeeService = dev.Workers.service.EmployeeService.getInstance();

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
                employeeId = id;
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
            if (shiftService.assignmentNeedsApproval(employeeId)) {
                approveAssignment();
            }

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

    private static void approveAssignment() {
        //int num = shiftService.numOfApprovalsNeeded(employeeId);
        //System.out.println("You have " + num + " pending replacements to approve");

        //Assignments assignment = shiftService.getNextPendingReplacement(employeeId);
        //System.out.println("You have pending replacement request for " + assignment.);
        System.out.println(shiftService.displayNextPendingAssignment(employeeId));
        System.out.println("Please enter 1 to approve or 0 to reject.");
        int choice = readIntSafe();

        if (choice == 1) {
            shiftService.processRequest(employeeId, true);
            System.out.println("Assignment request approved.");
        } else if (choice == 0) {
            shiftService.processRequest(employeeId, false);
            System.out.println("Assignment request rejected.");
        } else {
            System.out.println("Invalid choice. Try again.");
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
        Branch branch = employeeService.getEmployee(employeeId).getBranch();
        System.out.println(shiftService.displayCurrentWeek(branch));
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
        Branch branch = employeeService.getEmployee(employeeId).getBranch();
        System.out.println(shiftService.displayNextWeek(branch));
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
                    shiftType = MORNING;
                    break;
                case 2:
                    shiftType = EVENING;
                    break;
                case 3:
                    shiftType = ANY;
                    break;
                case 4:
                    shiftType = REST;
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
