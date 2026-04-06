package dev.Workers.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftRequirements {
    private Map<Shift, Map<Role, Integer>> Requirements;


    public ShiftRequirements() {
        Requirements = new HashMap<>();
    }

    public boolean isManagerExist(Shift shift){
        if (!Requirements.containsKey(shift)){
            return false;
        }
        Map<Role, Integer> innerMap = Requirements.get(shift);
        for (Role role :innerMap.keySet()){
            if(role.getRolename()=="Manager" && innerMap.get(role)>=1){
                return true;
            }
        }
        return false;
    }
    public void add(Shift shift,Role role,int count){
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
           throw new IllegalArgumentException("Count must be a non-negative number.");
       }
       if (role.getRolename().equals("Manager") && count == 0) {
           throw new IllegalArgumentException("There must be one manager on shift.");
       }
        if (!Requirements.containsKey(shift)){
            throw new IllegalArgumentException("Shift does not exist.");
        }
            if (count == 0) {
                Requirements.get(shift).remove(role);
            }
            else{
                Requirements.get(shift).put(role, count);
            }
        }
    }






