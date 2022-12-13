package me.raducapatina.server.data;

import java.util.Optional;

public interface Service<T> {

    T findById(Long id) throws Exception;

    void add(T element) throws Exception;

    void updateById(Long id) throws Exception;

    boolean deleteById(Long id) throws Exception;
}
