package com.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import com.todo.entity.Todo;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.todo.entity.QTodo.todo;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Todo> findAllSorted() {
        return queryFactory
                .selectFrom(todo)
                .orderBy(
                        todo.displayOrder.asc(),
                        todo.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Todo> findCompletedSorted(boolean completed) {
        return queryFactory
                .selectFrom(todo)
                .where(todo.completed.eq(completed))
                .orderBy(todo.displayOrder.asc())
                .fetch();
    }

    @Override
    public void deleteCompleted() {
        queryFactory
                .delete(todo)
                .where(todo.completed.eq(true))
                .execute();
    }
}
