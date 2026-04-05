package dev.Workers.domain;

import java.util.List;
public interface IListManager<T> {

    void addFullList(int id, List<T> items);

    void addSingleItem(int id, T item);

    void removeAll(int id);


    void removeSingleItem(int id, T item);

    List<T> getListById(int id);
}
