package com.todo.repository;

import com.todo.entity.Todo;

import java.util.List;

public interface TodoRepositoryCustom {
    List<Todo> findAllSorted(Long memberId);

    List<Todo> findCompletedSorted(Long memberId, boolean completed);

    void deleteCompleted(Long memberId);
}
