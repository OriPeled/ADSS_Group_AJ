package dev.Workers.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftRequirements {

    private Map<Shift, Map<Role, Integer>> Requirements;


    public ShiftRequirements() {
        Requirements = new HashMap<>();
    }


    private void add(Shift shift,Role role,int count){
        if(!Requirements.containsKey(shift)){
            Map<Role, Integer> newInnerMap = new HashMap<>();
            newInnerMap.put(new Role("Manager"), 1);
            Requirements.put(shift, newInnerMap);
        }
        if (role.getRolename().equals("Manager") && count < 1) {
            count = 1;
        }
        Requirements.get(shift).put(role, count);
    }

    public void update(Shift shift, Role role, int count){
        if (count < 0) {
            System.out.println("Count must be a non-negative number.");
       }
       if (role.getRolename().equals("Manager") && count == 0) {
           System.out.println("There must be one manager on shift.");
       }
        if (!Requirements.containsKey(shift)){
            add(shift, role, count);
            return;
        }
            if (count == 0) {
                Requirements.get(shift).remove(role);
            }
            else{
                Requirements.get(shift).put(role, count);
            }
        }

     public void remove(Shift shift){
        if (!Requirements.containsKey(shift)){
            System.out.println("Shift does not exist.");
        }
            Requirements.remove(shift);
        }

    }






