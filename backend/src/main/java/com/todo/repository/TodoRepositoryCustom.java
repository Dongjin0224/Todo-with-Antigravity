package com.todo.repository;

import com.todo.entity.Todo;

import java.util.List;

public interface TodoRepositoryCustom {
    List<Todo> findAllSorted();

    List<Todo> findCompletedSorted(boolean completed);

    void deleteCompleted();
}
