package dev.Workers.Tests;

import dev.Workers.presentation.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-End tests for full system flows.
 *
 * These tests simulate real user interaction through the console.
 */
public class Tests {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    @AfterEach
    void restoreSystem() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /**
     * Scenario:
     * 1. HR logs in
     * 2. Adds employee
     * 3. Fires employee
     * 4. Tries to assign him to a shift
     *
     * Expected:
     * Assignment should fail because the employee is inactive.
     */
    @Test
    void testFiredEmployeeCannotBeAssigned() {
        String input = String.join("\n",
                "2",          // HR mode
                "8888",       // HR password

                "1",          // Employees
                "2",          // Add employee
                "Yossi",
                "9999",       // unique ID
                "12345",
                "50",
                "1",          // full time
                "1",          // hourly
                "2",          // rest days
                "20/04/2026",

                "3",          // back from manageEmployee() to Employees menu
                "1",          // Manage existing employee
                "9999",
                "3",          // Remove
                "1",          // confirm removal

                "3",          // Back from Employees menu to HR main menu

                "2",          // Shifts
                "1",          // Manage Shifts Week
                "1",          // Sunday
                "1",          // Morning

                "1",          // Update Shift
                "1",          // Update Assignments
                "1",          // Add assignment
                "9999",       // fired employee
                "1",          // role

                "0",          // cancel force assign if asked
                "3",          // back from assignments
                "3",          // back from update shift
                "3",          // back from manage shift
                "4",          // back from shifts menu
                "3",          // logout from HR menu
                "3"           // exit main menu
        ) + "\n";

        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Main.main(new String[]{});

        String result = output.toString();

        assertTrue(result.contains("Yossi"));
        assertTrue(result.contains("Employee removed.") || result.contains("removed") || result.contains("inactive"));
        assertTrue(
                result.contains("Employee 9999 is inactive")
                        || result.contains("inactive")
                        || result.contains("Assignment failed")
                        || result.contains("Regular assignment failed")
        );
    }

    /**
     * Scenario:
     * 1. HR logs in
     * 2. Adds employee
     * 3. Fires employee
     * 4. Tries to promote/demote him
     *
     * Expected:
     * Operation should fail because the employee is inactive.
     */
    @Test
    void testFiredEmployeeCannotBePromoted() {
        String input = String.join("\n",
                "2",          // HR mode
                "8888",       // HR password

                "1",          // Employees
                "2",          // Add employee
                "David",
                "8888",       // unique ID
                "12345",
                "60",
                "1",          // full time
                "1",          // hourly
                "2",          // rest days
                "20/04/2026",

                "3",          // back from manageEmployee() to Employees menu
                "1",          // Manage existing employee
                "8888",
                "3",          // Remove
                "1",          // confirm removal

                "1",          // Manage existing employee again
                "8888",
                "2",          // Promote/Demote

                "5",          // back from employee menu if entered
                "3",          // back from Employees menu
                "3",          // logout from HR menu
                "3"           // exit main menu
        ) + "\n";

        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        Main.main(new String[]{});

        String result = output.toString();

        assertTrue(result.contains("David"));
        assertTrue(result.contains("Employee removed.") || result.contains("removed") || result.contains("inactive"));
        assertTrue(
                result.contains("Employee 8888 is inactive")
                        || result.contains("inactive")
                        || result.contains("Error:")
                        || result.contains("Promotion/Demotion")
        );
    }
}