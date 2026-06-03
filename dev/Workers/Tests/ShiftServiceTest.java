package dev.Workers.Tests;

import dev.Workers.service.ShiftService;
import dev.Workers.domain.*;
import dev.Workers.domain.Enums.*;
import dev.Workers.domain.Objects.*;

import dev.Workers.domain.Objects.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShiftService weekly scheduling and publishing flow.
 *
 * This class verifies:
 * - Publishing the current week
 * - Publishing the next week
 * - Publishing a specific week by date
 * - Week status changes after publication
 * - Publishing failure when no manager exists in the shift
 *
 * Important:
 * The system uses Singleton managers and services.
 * Therefore, each test resets all relevant singleton instances and shared maps
 * before running.
 */
public class ShiftServiceTest {

    private ShiftService shiftService;
    private EmployeeHandler employeeHandler;
    private ConstraintHandler constraintHandler;
    private ShiftHandler shiftHandler;
    private RoleRegistry roleRegistry;
    private BranchRegistry branchRegistry;

    Branch beerSheva = branchRegistry.getBranchByName("Beer-Sheva");

    private Role cashierRole;
    private Role storekeeperRole;

    /**
     * Resets all related singletons and shared data before each test.
     */
    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(ShiftService.class, "instance");
        resetSingleton(EmployeeHandler.class, "instance");
        resetSingleton(ConstraintHandler.class, "instance");
        resetSingleton(ShiftHandler.class, "instance");
        resetSingleton(RoleRegistry.class, "instance");

        employeeHandler = EmployeeHandler.getInstance();
        constraintHandler = ConstraintHandler.getInstance();
        shiftHandler = ShiftHandler.getInstance();
        shiftService = ShiftService.getInstance();
        roleRegistry = RoleRegistry.getInstance();
        cashierRole = roleRegistry.getRoleByName("Cashier");
        storekeeperRole = roleRegistry.getRoleByName("Storekeeper");

        clearStaticWeekSchedules();
    }

    /**
     * Resets a singleton static instance using reflection.
     *
     * @param clazz the singleton class
     * @param fieldName the static instance field name
     */
    private void resetSingleton(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, null);
    }

    /**
     * Clears the static weekSchedules map inside ShiftManager.
     */
    @SuppressWarnings("unchecked")
    private void clearStaticWeekSchedules() throws Exception {
        Field field = ShiftHandler.class.getDeclaredField("weekSchedules");
        field.setAccessible(true);
        ((Map<LocalDate, WeekSchedule>) field.get(null)).clear();
    }

    /**
     * Adds a valid employee to the system.
     *
     * @param id employee id
     */
    private void addEmployee(int id) {
        employeeHandler.add(
                "Employee" + id,
                id,
                branchRegistry.getBranchByName("Ofakim"),
                1000 + id,
                5000,
                new EmployeeTerms(JobStatus.fullTime, SalaryType.global, 2, DayOfWeek.WEDNESDAY),
                LocalDate.now()
        );
    }

    /**
     * Initializes default constraints for an employee.
     * By default, all days are set to ShiftType.any.
     *
     * @param id employee id
     */
    private void initConstraints(int id) {
        constraintHandler.initConstraintsForEmployee(id);
    }

    /**
     * Prepares two employees for a fully assigned week:
     * one cashier who is also marked as manager,
     * and one storekeeper.
     */
    private void prepareBasicWorkforce() {
        addEmployee(1);
        addEmployee(2);

        employeeHandler.getEmployee(1).setManager(true);

        employeeHandler.addRole(1, cashierRole);
        employeeHandler.addRole(2, storekeeperRole);

        initConstraints(1);
        initConstraints(2);
    }

    /**
     * Fills a full week (Sunday-Saturday, morning+evening)
     * with one cashier-manager and one storekeeper.
     *
     * @param sunday the Sunday of the target week
     */
    private void fillFullWeek(LocalDate sunday) {
        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);

            for (ShiftType type : new ShiftType[]{ShiftType.MORNING, ShiftType.EVENING}) {
                shiftService.addShift(beerSheva, date, type);
                Shift shift = shiftService.getShift(beerSheva, date, type);

                shiftService.setRequirement(shift, cashierRole, 1);
                shiftService.setRequirement(shift, storekeeperRole, 1);

                shiftService.assignEmployee(shift, cashierRole, 1);
                shiftService.assignEmployee(shift, storekeeperRole, 2);
            }
        }
    }

    /**
     * Reads the saved WeekSchedule object for a given week.
     *
     * @param dateInWeek any date inside the target week
     * @return the saved WeekSchedule, or null if not found
     */
    @SuppressWarnings("unchecked")
    private WeekSchedule getSavedWeek(LocalDate dateInWeek) throws Exception {
        Field field = ShiftHandler.class.getDeclaredField("weekSchedules");
        field.setAccessible(true);

        Map<LocalDate, WeekSchedule> map =
                (Map<LocalDate, WeekSchedule>) field.get(null);

        LocalDate sunday = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        return map.get(sunday);
    }

    /**
     * Verifies that publishing the current week succeeds
     * when all shifts are fully assigned and each shift has a manager.
     */
    @Test
    void publishWeekSchedule_shouldPublishCurrentWeek() throws Exception {
        LocalDate sunday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        prepareBasicWorkforce();
        fillFullWeek(sunday);

        shiftService.publishWeekByDate(beerSheva, sunday);

        WeekSchedule week = getSavedWeek(sunday);
        assertNotNull(week);
        assertTrue(week.isPublished());
    }

    /**
     * Verifies that publishing the next week succeeds
     * when the entire next week is fully assigned.
     */
    @Test
    void publishNextWeekSchedule_shouldPublishNextWeek() throws Exception {
        LocalDate sunday = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        prepareBasicWorkforce();
        fillFullWeek(sunday);

        shiftService.publishNextWeekSchedule(beerSheva);

        WeekSchedule week = getSavedWeek(sunday);
        assertNotNull(week);
        assertTrue(week.isPublished());
    }

    /**
     * Verifies that publishing a specific week by date works correctly.
     */
    @Test
    void publishWeekByDate_shouldPublishSpecificWeek() throws Exception {
        LocalDate sunday = LocalDate.of(2026, 4, 19);

        prepareBasicWorkforce();
        fillFullWeek(sunday);

        shiftService.publishWeekByDate(beerSheva, sunday.plusDays(3));

        WeekSchedule week = getSavedWeek(sunday);
        assertNotNull(week);
        assertTrue(week.isPublished());
    }

    /**
     * Verifies that after publishing a fully assigned week,
     * the week status becomes PUBLISHED.
     */
    @Test
    void getWeekStatus_shouldReturnPublishedAfterPublish() {
        LocalDate nextSunday = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        prepareBasicWorkforce();
        fillFullWeek(nextSunday);

        shiftService.publishNextWeekSchedule(beerSheva);

        assertEquals(WeekStatus.PUBLISHED, shiftService.getWeekStatus(beerSheva));
    }

    /**
     * Verifies that publishing fails when shifts do not contain any manager.
     *
     * In the current implementation, a shift is considered to have a manager
     * only if at least one assigned employee has employee.isManager() == true.
     */
    @Test
    void publishWeekSchedule_shouldFailWithoutManager() {
        LocalDate sunday = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        addEmployee(1);
        addEmployee(2);

        employeeHandler.addRole(1, cashierRole);
        employeeHandler.addRole(2, storekeeperRole);

        initConstraints(1);
        initConstraints(2);

        fillFullWeek(sunday);

        assertThrows(IllegalStateException.class, () ->
                shiftService.publishWeekByDate(beerSheva, sunday)
        );
    }
}