package dev.Workers.domain;

import java.util.*;

/**
 * מחלקת ShiftManager - אחראית על ניהול לוגיקת שיבוץ העובדים למשמרות.
 * מחלקה זו שייכת לשכבת ה-Domain Layer  ומממשת את הדרישות הפונקציונליות
 * ללא תלות בצורת האינטראקציה עם המשתמש.
 */
public class ShiftManager {

    /**
     * hrService - ממשק המייצג את שירות משאבי האנוש.
     * שימוש ב-Dependency Injection (הזרקת תלות) מאפשר הפרדת אחריות:
     * המנהל לא יוצר אובייקטים חדשים אלא משתמש במידע קיים בזיכרון[cite: 67].
     */
    private final IHRService hrService;

    /**
     * assignments - מבנה הנתונים לשיבוצים (במקום בסיס נתונים)[cite: 91].
     * המבנה מקשר בין משמרת (Shift) לבין מפה פנימית של תפקידים (Role)
     * ורשימת מזהי העובדים (IDs) המשובצים לכל תפקיד.
     */
    private Map<Shift, Map<Role, List<Integer>>> assignments;

    /**
     * בנאי המחלקה - מקבל את שירות ה-HR מבחוץ.
     *
     * @param hrService מופע המממש את ממשק ה-HR (מוזרק ב-Main).
     */
    public ShiftManager(IHRService hrService) {
        this.hrService = hrService;
        this.assignments = new HashMap<>();
    }

    /**
     * פונקציית assignToShift - מבצעת שיבוץ עובד למשמרת.
     * הפונקציה בודקת עמידה בדרישות הפונקציונליות לפני השיבוץ.
     * * @param employeeId מזהה העובד (ID) לשיבוץ.
     *
     * @param shift אובייקט המשמרת המיועדת.
     * @param role  התפקיד שהעובד אמור לבצע במשמרת.
     */
    public void assignToShift(int employeeId, Shift shift, Role role) {

        // בדיקת דרישה פונקציונלית דרך ה-Domain Layer:
        // וידוא שהעובד אכן מוסמך (Qualified) לתפקיד המבוקש[cite: 39].
        if (hrService.isEmployeeQualified(employeeId, role)) {

            // עדכון מבנה הנתונים בזיכרון בשיטת computeIfAbsent למניעת שגיאות Null
            assignments.computeIfAbsent(shift, k -> new HashMap<>())
                    .computeIfAbsent(role, k -> new ArrayList<>())
                    .add(employeeId);

            System.out.println("עובד " + employeeId + " שובץ בהצלחה כ-" + role + " במשמרת.");
        } else {
            // טיפול במקרה של אי-עמידה בדרישה (מתאים לדרישות התיקוף במטלה)
            System.out.println("שגיאה: העובד אינו מוסמך לתפקיד זה.");
        }
    }

    /**
     * מחזירה את כל השיבוצים הקיימים.
     * משמש לצורך Traceability (עקיבות) מול תרשים המחלקות.
     *
     * @return מפת השיבוצים הנוכחית בזיכרון.
     */
    public Map<Shift, Map<Role, List<Integer>>> getAssignments() {
        return assignments;
    }

    public List<Shift> getShiftHistory() {
        // שליפת כל מפתחות המשמרות מתוך מפת השיבוצים
        List<Shift> history = new ArrayList<>(assignments.keySet());

        // מומלץ למיין את ההיסטוריה לפי תאריך/זמן אם מחלקת Shift תומכת בכך (Comparable)
        // Collections.sort(history); 

        return history;

    }
}