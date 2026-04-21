package dev.Workers.presentation;

import dev.Workers.Service.ConstraintService;
import dev.Workers.Service.EmployeeService;
import dev.Workers.Service.RoleService;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.SalaryType;

import dev.Workers.domain.Objects.EmployeeTerms;

import java.time.LocalDate;

import static dev.Workers.presentation.Main.scanner;
import static dev.Workers.presentation.Parser.*;

public class ManageEmployeesMenu  {
    static EmployeeService employeeService = EmployeeService.getInstance();
    static ConstraintService constraintService = ConstraintService.getInstance();
    static RoleService roleService = RoleService.getInstance();

    public static void start() {
        while (true) {
            printMainMenu();
            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> accessEmployee();
                    case 2 -> addEmployeeMenu();
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static void accessEmployee() {
        System.out.println("Enter employee ID (0 to go back):");

        int empId = readIntSafe();

        if (empId == 0) return;

        if (!employeeService.exists(empId)) {
            System.out.println("Employee not found.");
            return;
        }
        manageEmployee(empId);
    }

    public static void manageEmployee(int empId) {
        while (true) {
            System.out.println(employeeService.getEmployeeName(empId) + " (" + empId + ")");
            printManageEmployeeMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> details(empId);
                case 2 -> roles(empId);
                case 3 -> promoteDemote(empId);
                case 4 -> {
                    fire(empId);
                    return;}
                case 5 -> {
                    rehire(empId);
                    return;}
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void promoteDemote(int empId) {
        employeeService.promoteDemote(empId);
    }

    /**
     * Employee details management menu.
     * Allows updating name, bank account, salary, or terms.
     *
     * @param empId employee ID
     */
    private static void details(int empId) {
        while (true) {
            System.out.println("======================================");
            System.out.println(employeeService.getEmployeeDetails(empId));
            System.out.println("======================================");

            printDetailsMenu();

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> updateName(empId);
                case 2 -> updateBankAccount(empId);
                case 3 -> updateSalary(empId);
                case 4 -> updateTerms(empId);
                case 0 -> {return;}
                default -> System.out.println("Invalid choice. Please select a valid option (0-4).");
            }
        }
    }

    private static void updateName(int empId) {
        while (true) {
            System.out.println("Please enter a new employee name (or 0 to cancel):");
            String name = scanner.nextLine();

            if (name.equals("0")) return;

            try {
                employeeService.updateName(empId, name);
                System.out.println("Employee name updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void updateBankAccount(int empId) {
        while (true) {
            System.out.println("Please enter a new bank account number (or 0 to cancel):");
            int bank = readIntSafe();

            if (bank == 0) return;

            try {
                employeeService.updateBankAccount(empId, bank);
                System.out.println("Bank account updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void updateSalary(int empId) {
        while (true) {
            System.out.println("Please enter a new salary (or 0 to cancel):");
            double salary = readDoubleSafe();

            if (salary == 0) return;

            try {
                employeeService.updateSalary(empId, salary);
                System.out.println("Salary updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Handles updating employee employment terms:
     * job status, salary type, and rest days.
     *
     * @param empId employee ID
     */
    public static void updateTerms(int empId) {
        while (true) {

            System.out.println("======================================");
            System.out.println(employeeService.getEmployeeTermsDisplay(empId));
            System.out.println("======================================");

            printUpdateTermsMenu();
            int choice = readIntSafe();
            switch (choice) {
                case 1 -> changeJobStatus(empId);
                case 2 -> changeSalaryType(empId);
                case 3 -> changeRestDays(empId);
                case 0 -> {return;}
                default -> System.out.println("Invalid choice. Please select 0-3.");
            }

            System.out.println();
        }
    }

    /**
     * Changes employee job status after user confirmation.
     */
    private static void changeJobStatus(int empId) {
        System.out.println("Change job status?");
        System.out.println("1 - confirm, 0 - cancel");

        int choice = readIntSafe();
        switch (choice) {
            case 1 -> {
                try {
                    employeeService.updateJobStatus(empId);
                    System.out.println("Job status updated successfully.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            case 0 -> {}
            default -> System.out.println("Invalid input.");
        }
    }

    /**
     * Changes employee salary type after confirmation.
     */
    private static void changeSalaryType(int empId) {
        System.out.println("Change salary type?");
        System.out.println("1 - confirm, 0 - cancel");

        int choice = readIntSafe();
        switch (choice) {
            case 1 -> {
                try {
                    employeeService.updateSalaryType(empId);
                    System.out.println("Salary type updated successfully.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }
            case 0 -> {}
            default -> System.out.println("Invalid input.");
        }
    }

    /**
     * Updates employee rest days (1–7).
     */
    private static void changeRestDays(int empId) {
        System.out.println("Enter number of rest days (1-7, 0 to cancel):");
        int days = readIntSafe();

        if (days == 0) return;

        if (days < 1 || days > 7) {
            System.out.println("Invalid range. Must be 1-7.");
            return;
        }

        try {
            employeeService.updateRestDays(empId, days);
            System.out.println("Rest days updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void roles(int empId) {
        while (true) {
            System.out.println("======================================");
            System.out.println("Existing employee roles:");
            System.out.println(roleService.getEmployeeRoles(empId));

            System.out.println("1. Add Role");
            System.out.println("2. Remove Role");
            System.out.println("0. Back");

            int choice = readIntSafe();
            switch (choice) {
                case 1 -> addRole(empId);
                case 2 -> removeRole(empId);
                case 0 -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Adds a role to a specific employee.
     * Shows available roles, validates input, and handles errors safely.
     *
     * @param empId employee ID
     */
    private static void addRole(int empId) {
        Role[] roles = Role.values();
        while (true) {
            System.out.println("======================================");
            System.out.println("Existing employee roles:");
            System.out.println(roleService.getEmployeeRoles(empId));

            System.out.println("======================================");
            System.out.println("Available roles to assign:");
            System.out.println(roleService.getFormattedAvailableRoles(empId));
            System.out.println("Please choose a role to add (0 to cancel):");

            int choice = readIntSafe();

            if (choice == 0) {
                return;
            }

            Role selectedRole = getRoleFromNumber(choice+1);

            try {
                roleService.addRoleToEmployee(empId, selectedRole);
                System.out.println("Role '" + selectedRole +
                        "' successfully added to employee " +
                        employeeService.getEmployeeName(empId));
                return;

            } catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }

    private static void removeRole(int empId) {
        Role[] roles = Role.values();
        while (true) {
            System.out.println("======================================");
            System.out.println("Existing employee roles:");
            System.out.println(roleService.getEmployeeRoles(empId));

            System.out.println("Choose a role to remove (0 to cancel):");

            int choice = readIntSafe();

            if (choice == 0) {
                return;
            }

            Role selectedRole = getRoleFromNumber(choice);

            try {
                roleService.removeSpecificRole(empId, selectedRole);
                System.out.println("Role '" + selectedRole +
                        "' successfully removed");
                return;

            } catch (Exception e) {
                System.out.println("Failed to remove role: " + e.getMessage());
                return;
            }
        }
    }

    private static void fire(int empId) {
        System.out.println("Are you sure you want to fire " +
                employeeService.getEmployeeName(empId) +
                " (" + empId + ")? Enter 1 to confirm, 0 to cancel:");

        int choice = readIntSafe();

        switch (choice) {
            case 1 -> {
                try {
                    employeeService.fire(empId);
                    System.out.println("Employee fired.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            case 0 -> {}
            default -> System.out.println("Invalid input.");
        }
    }

    private static void rehire(int empId) {
        System.out.println("Enter employee ID or 0 to cancel.");
        int id = readIntSafe();

        if (id == 0) return;

        try {
            employeeService.rehire(empId);
            System.out.println("Employee rehired.");
        } catch (Exception e) {
            System.out.println(e.getMessage());;
        }
    }

    /**
     * Displays the menu to add a new employee to the system.
     * Validates unique ID and ensures proper input stream handling.
     */
    /**
     * Handles the menu for adding a new employee.
     * This method ensures the input buffer is cleared and the class-level ID is updated correctly.
     */
    private static void addEmployeeMenu() {
        System.out.println("=== Add New Employee ===");

        String name;
        while (true) {
            System.out.println("Enter name (or '0' to go back):");
            name = scanner.nextLine();

            if (name.equals("0")) return;

            if (!name.trim().isEmpty()) break;

            System.out.println("Invalid name. Try again.");
        }

        int id;
        while (true) {
            System.out.println("Please enter the employee ID to continue ");
            id = readIntSafe();

            if (id <= 0) {
                System.out.println("ID must be positive.");
                continue;
            }

            if (employeeService.exists(id)) {
                System.out.println("Employee already exists.");
                continue;
            }
            break;
        }

        int bankAccount;
        while (true) {
            System.out.println("Please enter the bank account number:");
            bankAccount = readIntSafe();

            if (bankAccount <= 0) {
                System.out.println("Bank account must be positive.");
                continue;
            }

            break;
        }

        double salary;
        while (true) {
            System.out.println("Please enter the employee salary:");
            salary = readDoubleSafe();

            if (salary <= 0) {
                System.out.println("Salary must be positive.");
                continue;
            }

            break;
        }

        EmployeeTerms terms = createEmployeeTermsFromInput();

        LocalDate date;
        while (true) {
            System.out.println("Please enter the start date (format: dd/MM/yyyy):");
            try {
                String input = scanner.nextLine();
                date = Parser.stringToDate(input);

                if (date == null) {
                    System.out.println("Invalid date format.");
                    continue;
                }

                if (date.isBefore(LocalDate.now())) {
                    System.out.println("Start date cannot be in the past. Please enter today or a future date.");
                    continue;
                }
                break;

            } catch (Exception e) {
                System.out.println("Invalid date. Try again.");
            }
        }

        addEmployee(name, id, bankAccount, salary, terms, date);
    }

    /**
     *
     * @param name
     * @param ID
     * @param bankAccount
     * @param salary
     * @param startDate
     * helper to add all emp details
     */
    private static void addEmployee(String name, int ID, int bankAccount, double salary,
                                    EmployeeTerms terms, LocalDate startDate) {
        try {
            employeeService.add(name, ID, bankAccount, salary, terms, startDate);
            constraintService.initConstraintForEmployee(ID);
            System.out.println("Success: Employee added successfully.");
            manageEmployee(ID);

        } catch (IllegalArgumentException e) {

            System.out.println("Validation Error: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("General Error: " + e.getMessage());
        }
    }

    private static EmployeeTerms createEmployeeTermsFromInput() {
        System.out.println("--- Employment Terms ---");

        JobStatus jobStatus;
        while (true) {
            System.out.println("Select Job Status (1. Full Time, 2. Half Time):");
            int jobChoice = readIntSafe();

            if (jobChoice == 1) {
                jobStatus = JobStatus.fullTime;
                break;
            } else if (jobChoice == 2) {
                jobStatus = JobStatus.halfTime;
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }

        SalaryType salaryType;
        while (true) {
            System.out.println("Select Salary Type (1. Hourly, 2. Global):");
            int salaryChoice = readIntSafe();

            if (salaryChoice == 1) {
                salaryType = SalaryType.hourly;
                break;
            } else if (salaryChoice == 2) {
                salaryType = SalaryType.global;
                break;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }

        int restDays;
        while (true) {
            System.out.println("Enter number of rest days (1-7):");
            restDays = readIntSafe();

            if (restDays >= 1 && restDays <= 7) {
                break;
            } else {
                System.out.println("Rest days must be between 1 and 7.");
            }
        }
        return new EmployeeTerms(jobStatus, salaryType, restDays);
    }

    public static void printMainMenu() {
        System.out.println("Employees");
        System.out.println("1. Manage existing Employee");
        System.out.println("2. Add Employee");
        System.out.println("0. Back");
    }

    public static void printManageEmployeeMenu() {
        System.out.println("1. Employee Details");
        System.out.println("2. Employee Roles");
        System.out.println("3. Promote/Demote");
        System.out.println("4. Fire Employee");
        System.out.println("5. Rehire Employee");
        System.out.println("0. Back");
    }

    public static void printDetailsMenu() {
        System.out.println("Please choose an action:");
        System.out.println("1. Update employee name");
        System.out.println("2. Update bank account number");
        System.out.println("3. Update salary");
        System.out.println("4. Update employment terms");
        System.out.println("0. Back");
    }

    public static void printUpdateTermsMenu() {
        System.out.println("1. Change job status");
        System.out.println("2. Change salary type");
        System.out.println("3. Change number of rest days");
        System.out.println("0. Back");
    }
}