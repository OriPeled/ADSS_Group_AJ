package dev.Workers.domain;

import java.util.Date;

public interface IManager <T>{


    void add(int id,T t);
    void remove(int id);
    T getById(int id);

}
