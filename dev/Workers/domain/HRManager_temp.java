package dev.Workers.domain;


import java.util.*;

/**
 * מחלקת HRManager - מנהלת את מאגר העובדים וההסמכות שלהם.
 * ממומשת כ-Singleton כדי להבטיח מופע יחיד בזיכרון ה-RAM (ללא Database).
 */
public class HRManager_temp implements IHRService {

    // 1. זהו המשתנה הסטטי שיחזיק את המופע היחיד.
    // חשוב: למחוק כל import שקשור ל-sun.awt או PixelConverter!
    private static HRManager_temp instance;

    // מאגרי הנתונים בזיכרון
    private Map<Integer, Employee> employees;
    private Map<Integer, List<Role>> employeeRoles;

    // 2. בנאי פרטי - מבטיח שאף אחד לא יוכל לעשות new HRManager() מבחוץ
    private HRManager_temp() {
        this.employees = new HashMap<>();
        this.employeeRoles = new HashMap<>();
    }

    // 3. המתודה הסטטית לקבלת המופע
    public static HRManager_temp getInstance() {
        if (instance == null) {
            instance = new HRManager_temp();
        }
        return instance;
    }

    @Override
    public void addEmployee(String name, int id, int bankAccount, double salary, String terms, Date startDate) {
        if (!employees.containsKey(id)) {
            Employee newEmp = new Employee(name, id, bankAccount, salary, terms, startDate);
            employees.put(id, newEmp);
            employeeRoles.put(id, new ArrayList<>());
        }
    }

    @Override
    public Employee getEmployeeById(int id) {
        return employees.get(id);
    }

    @Override
    public boolean isEmployeeQualified(int id, Role role) {
        List<Role> roles = employeeRoles.get(id);
        return roles != null && roles.contains(role);
    }

    public void addRoleToEmployee(int id, Role role) {
        if (employeeRoles.containsKey(id)) {
            if (!employeeRoles.get(id).contains(role)) {
                employeeRoles.get(id).add(role);
            }
        }
    }
}