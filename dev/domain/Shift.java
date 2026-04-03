package dev.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Shift {
    private shiftType shift;
    private Date shiftDate;
    private Map<Role, Integer> requirements;
    private  Map<Role, List<Employee>> employee;
}
