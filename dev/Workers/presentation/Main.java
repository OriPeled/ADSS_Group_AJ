package dev.Workers.presentation;
import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===HR Manager Mode===");
        while (true) {
            displayMenu();
        }
    }

    public static void displayMenu() {
        System.out.println("Choose to Manage:");
        System.out.println("1. Employees");
        System.out.println("2. Shifts");
        System.out.println("3. Exit");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                EmployeesMenu.start();
            case 2:
                ShiftsMenu.start();
            case 3:
                System.out.println("Have a good day.");
                //scanner.close();
                //break;
                System.exit(0);
            default:
                System.out.println("Invalid input.");
        }
    }
}