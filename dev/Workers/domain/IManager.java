package dev.Workers.domain;

import java.util.Date;

public interface IManager <T>{


    void add(T employee);
    void remove(int id);
    T getById(int id);

}
