package me.raducapatina.server.data;

public interface Service<T> {

    T findById(Long id) throws Exception;

    void add(T element) throws Exception;

    void update(T element) throws Exception;

    boolean deleteById(Long id) throws Exception;
}
