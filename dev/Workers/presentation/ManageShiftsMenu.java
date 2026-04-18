package dev.Workers.presentation;

import dev.Workers.Service.ConstraintService;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.ShiftType;
import dev.Workers.domain.Objects.Shift;
import dev.Workers.Service.ShiftService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static dev.Workers.domain.Enums.ShiftType.evening;
import static dev.Workers.domain.Enums.ShiftType.morning;
import static dev.Workers.domain.Enums.WeekStatus.READY_TO_PUBLISH;
import static dev.Workers.presentation.Main.scanner;

public class ManageShiftsMenu {
    static ShiftService shiftService = ShiftService.getInstance();
    static ConstraintService constraintService = ConstraintService.getInstance();
    static Shift shift;
    //static int id;

    public static void start() {
        // sunday morning - FULL
        // sunday evening IN-PROCESS
        // monday morning EMPTY
        // ...
        // saturday evening FULL
        while (true) {
            System.out.println("1. Manage Shifts Week");
            System.out.println("2. Get Shifts History");
            System.out.println("3. Update Constraints Deadline");
            System.out.println("4. Back");

            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    manageShiftsWeek();
                    break;
                case 2:
                    getShiftsHistory();
                    break;
                case 3:
                    updateDeadline();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static boolean handlePublishMenu() {
        System.out.println("All shifts assigned. Do you wish to public the week schedule?");
        while (true) {
            System.out.println("Enter 1 to publish or 0 to continue managing the schedule.");
            String input = scanner.nextLine();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 0:
                    return false;
                case 1:
                    shiftService.publishWeekSchedule();
                    System.out.println("Shifts schedule published.");
                    return true;
                default:
            }
        }
    }

    private static void manageShiftsWeek() {
        while (true) {
            if (shiftService.getWeekStatus() == READY_TO_PUBLISH) {
                if (handlePublishMenu()) return;
            }
            System.out.println(shiftService.displayWeekAssignments());
            System.out.println("Manage Shifts Week");

            System.out.println("1. Enter Day (1-7) or 0 to go back");
            int dayNumber;
            try {
                dayNumber = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                continue;
            }

            if (dayNumber == 0) return;

            LocalDate date;
            try {
                date = Parser.getDateOfNextWeekFromNumber(dayNumber);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input. Day must be between 1 and 7.");
                continue;
            }

            System.out.println("Enter Shift (1 for morning, 2 for evening) or 0 to go back");
            int typeNumber;
            try {
                typeNumber = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input");
                continue;
            }

            if (typeNumber == 0) return;

            ShiftType shiftType;
            switch (typeNumber) {
                case 1 -> shiftType = morning;
                case 2 -> shiftType = evening;
                default -> {
                    System.out.println("Invalid shift type.");
                    return;
                }
            }
            shift = shiftService.getShift(date, shiftType);
            manageShift();
        }
    }

    private static void manageShift() {
        while (true) {
            System.out.println();
            System.out.println("1. Update Shift");
            System.out.println("2. Remove Shift");
            System.out.println("3. Back");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    updateShift();
                    break;
                case 2:
                    removeShift();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void removeShift() {
        while (true) {
            System.out.println("Are you sure you want to remove this shift? If yes, enter 1, else enter 0.");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    shiftService.removeShift(shift);
                    System.out.println("Shift successfully removed.");
                    return;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void updateShift() {
        while (true) {
            System.out.println(shiftService.getShiftDetails(shift));
            System.out.println("1. Update Assignments");
            System.out.println("2. Update Requirements");
            System.out.println("3. Back");

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
                    updateAssignments();
                    break;
                case 2:
                    updateRequirements();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }


    public static void updateAssignments() {
        while (true) {
            System.out.println(shiftService.getShiftDetails(shift));
            System.out.println("1. Add assignment");
            System.out.println("2. Make a replacement");
            System.out.println("3. Back");

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
                    addAssignment();
                    break;
                case 2:
                    replace();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid input.");
            }
        }
    }

    private static void addAssignment() {
        int id = -1;
        Role role = null;

        try {
            System.out.println(shiftService.getAvailableEmployeesForShift(shift));

            System.out.println("Enter Employee ID:");
            id = Integer.parseInt(scanner.nextLine());

            System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager):");
            int roleNumber = Integer.parseInt(scanner.nextLine());

            if (roleNumber < 1 || roleNumber > Role.values().length) {
                System.out.println("Invalid role choice.");
                return;
            }

            role = Role.values()[roleNumber - 1];

            /*boolean roleNeeded = shiftService.isRoleNeeded(shift, role);
            if (!roleNeeded) {
                System.out.println("Staffing shortage detected for " + role + ".");
                forceAssign(shift, role, id);
                return;
            }*/

            shiftService.assignEmployee(shift, role, id);
            System.out.println("Employee assigned successfully.");

        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());

        } catch (RuntimeException e) {
            System.out.println("Regular assignment failed: " + e.getMessage());

            if (role != null && id != -1) {
                forceAssign(shift, role, id);
            }
        }
    }

    private static void replace() {
        System.out.println("Enter the ID of the already assigned employee");
        int currentEmployeeId = 0;
        try {
            currentEmployeeId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }

        System.out.println("Enter the ID of the employee to replace him");
        int newEmployeeId = 0;
        try {
            newEmployeeId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }

        shiftService.replaceEmployee(shift, currentEmployeeId, newEmployeeId);
        System.out.println("Replacement successful.");
    }

    private static void updateRequirements() {
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int roleNumber = Integer.parseInt(scanner.nextLine());
        Role role = Role.values()[roleNumber - 1];
        System.out.println("Enter new amount.");
        int amount = Integer.parseInt(scanner.nextLine());
        shiftService.setRequirement(shift, role, amount);
        System.out.println("Amount updated.");
    }

    private static void updateDeadline() {
        System.out.println("Current deadline: " + constraintService.getDeadline());
        System.out.print("Enter new day (2-7) or 0 to go back:");

        String dayNumber = scanner.nextLine();
        if (dayNumber.equals("0")) return;

        try {
            DayOfWeek newDay = Parser.getDayFromNumber(Integer.parseInt(dayNumber));
            constraintService.setDeadline(newDay);
            System.out.println("Deadline updated to " + newDay);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid format.");
        }
    }

    public static void getShiftsHistory() {
        System.out.println(shiftService.getShiftHistory());
    }

    private static void forceAssign(Shift shift, Role role, int id) {

        while (true) {
            System.out.println("Are you sure you want to force assign this employee?");
            System.out.println("Press 1 to confirm, 0 to cancel:");

            String input = scanner.nextLine();

            if (input.equals("0")) {
                System.out.println("Operation cancelled.");
                return;
            }

            if (input.equals("1")) {
                try {
                    shiftService.forceAssignEmployee(shift, role, id);
                    System.out.println("Employee assigned via special approval.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                return;
            }

            System.out.println("Invalid input. Please enter 1 or 0.");
        }
    }
    /*public static void assignEmployee() {
        System.out.println("Enter role (1 for cashier, 2 for housekeeper, 3 for manager)");
        int choice = Integer.parseInt(scanner.nextLine());
        shiftService.assignEmployee(shift, choice, id);
    }

    private static void removeShift() {
        System.out.println("Are you sure you want to remove " + shift.getType() + " (" + shift.getShiftDate() + ")?" +
                           "If yes - enter 1, else 0.");

        int choice = Integer.parseInt(scanner.nextLine());
        switch (choice) {
            case 1:
                shiftService.removeShift(shift);
                System.out.println("Shift removed.");
                manageShift();
            case 2:
                manageShift();
        }
    }

    private static void addShift() {
        System.out.print("Enter date (dd/mm/yyyy) or 0 to go back:");
        int input = Integer.parseInt(scanner.nextLine());
        if (input == 0)
            start();
        String dateString = String.valueOf(input);
        LocalDate date = Parser.stringToDate(dateString);

        System.out.print("Enter type (morning/evening):");
        shiftType type = shiftType.valueOf(scanner.nextLine());

        shiftService.addShift(date, type);
        shift = shiftService.getShift(date, type);
        System.out.println("Shift added.");
        updateShift();
    }*/
}
