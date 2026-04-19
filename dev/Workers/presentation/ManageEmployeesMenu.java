package dev.Workers.presentation;

import dev.Workers.Service.ConstraintService;
import dev.Workers.Service.EmployeeService;
import dev.Workers.Service.RoleService;
import dev.Workers.domain.Enums.JobStatus;
import dev.Workers.domain.Enums.Role;
import dev.Workers.domain.Enums.SalaryType;

import dev.Workers.domain.EmployeeTerms;

import java.time.LocalDate;

import static dev.Workers.presentation.Main.scanner;
import static dev.Workers.presentation.Parser.readDoubleSafe;
import static dev.Workers.presentation.Parser.readIntSafe;

public class ManageEmployeesMenu {
    static EmployeeService employeeService = EmployeeService.getInstance();
    static ConstraintService constraintService = ConstraintService.getInstance();
    static RoleService roleService = RoleService.getInstance();


    public static void start() {
        while (true) {
            System.out.println("Employees");
            System.out.println("1. Manage existing Employee");
            System.out.println("2. Add Employee");
            System.out.println("0. Back");
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

        try {
            int empId = Integer.parseInt(scanner.nextLine());

            if (empId == 0) return;

            if (!employeeService.exists(empId)) {
                System.out.println("Employee not found.");
                return;
            }
            manageEmployee(empId);

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    public static void manageEmployee(int empId) {
        while (true) {
            System.out.println(employeeService.getEmployeeName(empId) + " (" + empId + ")");
            System.out.println("1. Employee Details");
            System.out.println("2. Employee Roles");
            System.out.println("3. Remove Employee");
            System.out.println("0. Back");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> details(empId);
                    case 2 -> roles(empId);
                    case 3 -> {
                        remove(empId);
                        return;}
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
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

            System.out.println("""
                Please choose an action:
                1 - Update employee name
                2 - Update bank account number
                3 - Update salary
                4 - Update employment terms
                0 - Back to previous menu
                """);

            int choice = readIntSafe();

            switch (choice) {
                case 1 -> updateName(empId);
                case 2 -> updateBankAccount(empId);
                case 3 -> updateSalary(empId);
                case 4 -> updateTerms(empId);
                case 0 -> {return;}
                default -> System.out.println("Invalid choice. Please select a valid option (0-4).");
            }

            System.out.println();
        }
    }

    private static void updateName(int empId) {
        while (true) {
            System.out.println("Please enter a new employee name (or 0 to cancel):");

            String name = scanner.nextLine();

            if (name.equals("0")) return;

            if (name.trim().isEmpty()) {
                System.out.println("Employee name cannot be empty.");
                continue;
            }

            try {
                employeeService.updateName(empId, name);
                System.out.println("Employee name updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void updateBankAccount(int empId) {
        while (true) {
            System.out.println("Please enter a new bank account number (or 0 to cancel):");

            int bank = readIntSafe();

            if (bank == 0) return;

            if (bank <= 0) {
                System.out.println("Bank account must be positive.");
                continue;
            }

            try {
                employeeService.updateBankAccount(empId, bank);
                System.out.println("Bank account updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void updateSalary(int empId) {
        while (true) {
            System.out.println("Please enter a new salary (or 0 to cancel):");

            double salary = readDoubleSafe();

            if (salary == 0) return;

            if (salary <= 0) {
                System.out.println("Salary must be positive.");
                continue;
            }

            try {
                employeeService.updateSalary(empId, salary);
                System.out.println("Salary updated successfully.");
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
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

            System.out.println("""
                Please choose an option to update:
                1 - Change job status
                2 - Change salary type
                3 - Change number of rest days
                0 - Back
                """);

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

        if (choice != 1) return;

        try {
            employeeService.updateJobStatus(empId);
            System.out.println("Job status updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Changes employee salary type after confirmation.
     */
    private static void changeSalaryType(int empId) {
        System.out.println("Change salary type?");
        System.out.println("1 - confirm, 0 - cancel");

        int choice = readIntSafe();

        if (choice != 1) return;

        try {
            employeeService.updateSalaryType(empId);
            System.out.println("Salary type updated successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
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
            System.out.println("Error: " + e.getMessage());
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

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> addRole(empId);
                    case 2 -> removeRole(empId);
                    case 0 -> { return; }
                    default -> System.out.println("Invalid choice.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
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

            if (choice < 1 || choice > roles.length) {
                System.out.println("Invalid choice. Please select a valid role number.");
                continue;
            }

            Role selectedRole = roles[choice - 1];

            try {
                roleService.addRoleToEmployee(empId, selectedRole);
                System.out.println("Role '" + selectedRole +
                        "' successfully added to employee " +
                        employeeService.getEmployeeName(empId));
                return;

            } catch (Exception e) {
                System.out.println("Failed to add role: " + e.getMessage());
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

            if (choice < 1 || choice > roles.length) {
                System.out.println("Invalid choice. Please select a valid role number.");
                continue;
            }

            Role selectedRole = roles[choice - 1];

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

    /*private static void promoteDemote(int empId) {
        try {
            roleService.promoteDemote(empId);
            System.out.println("Promotion/Demotion applied.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }*/

    private static void remove(int empId) {
        System.out.println("Are you sure you want to remove " +
                employeeService.getEmployeeName(empId) +
                " (" + empId + ")? Enter 1 to confirm, 0 to cancel:");

        int choice = readIntSafe();

        if (choice != 1) return;

        try {
            employeeService.remove(empId);
            System.out.println("Employee removed.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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

        int Id;
        while (true) {
            System.out.println("Please enter the employee ID to continue ");
            try {
                Id = Integer.parseInt(scanner.nextLine());

                if (Id <= 0) {
                    System.out.println("ID must be positive.");
                    continue;
                }

                if (employeeService.exists(Id)) {
                    System.out.println("Employee already exists.");
                    continue;
                }
                break;

            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        int bankAccount;
        while (true) {
            System.out.println("Please enter the bank account number:");
            try {
                bankAccount = Integer.parseInt(scanner.nextLine());

                if (bankAccount <= 0) {
                    System.out.println("Bank account must be positive.");
                    continue;
                }

                break;

            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        double salary;
        while (true) {
            System.out.println("Please enter the employee salary:");
            try {
                salary = Double.parseDouble(scanner.nextLine());

                if (salary <= 0) {
                    System.out.println("Salary must be positive.");
                    continue;
                }

                break;

            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
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

        addEmployee(name, Id, bankAccount, salary, terms, date);
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
            try {
                int jobChoice = Integer.parseInt(scanner.nextLine());

                if (jobChoice == 1) {
                    jobStatus = JobStatus.fullTime;
                    break;
                } else if (jobChoice == 2) {
                    jobStatus = JobStatus.halfTime;
                    break;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Input must be a number. Try again.");
            }
        }

        SalaryType salaryType;
        while (true) {
            System.out.println("Select Salary Type (1. Hourly, 2. Global):");
            try {
                int salaryChoice = Integer.parseInt(scanner.nextLine());

                if (salaryChoice == 1) {
                    salaryType = SalaryType.hourly;
                    break;
                } else if (salaryChoice == 2) {
                    salaryType = SalaryType.global;
                    break;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Input must be a number. Try again.");
            }
        }

        int restDays;
        while (true) {
            System.out.println("Enter number of rest days (1-7):");
            try {
                restDays = Integer.parseInt(scanner.nextLine());

                if (restDays >= 1 && restDays <= 7) {
                    break;
                } else {
                    System.out.println("Rest days must be between 1 and 7.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Input must be a number. Try again.");
            }
        }
        return new EmployeeTerms(jobStatus, salaryType, restDays);
    }
}