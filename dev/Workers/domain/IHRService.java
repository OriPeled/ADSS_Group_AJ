package dev.Workers.domain;

public interface IHRService {
    void addEmployee(String name, int id, int bankAccount, double salary, String terms, java.util.Date startDate);
    Employee getEmployeeById(int id);
    boolean isEmployeeQualified(int id, Role role);

}
