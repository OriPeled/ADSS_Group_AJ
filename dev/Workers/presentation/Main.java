package dev.Workers.presentation;
import dev.Workers.setup.DataInitializer;

import java.util.Scanner;

public class Main {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        DataInitializer.initSystem();
        while (true) {
            displayMenu();
        }
    }

    public static void displayMenu() {
        System.out.println("Choose Mode:");
        System.out.println("1. User Mode");
        System.out.println("2. HR Manager Mode");
        System.out.println("3. Exit");

        if (scanner.hasNextInt()) {
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    UserMode.login();
                    break;
                case 2:
                    AdminMode.login();
                    break;
                case 3:
                    System.out.println("Have a good day.");
                    //scanner.close();
                    //break;
                    System.exit(0);
                default:
                    System.out.println("Invalid input.");
            }
        } else {
            System.out.println("Invalid input.");
            scanner.next();
        }
    }
}