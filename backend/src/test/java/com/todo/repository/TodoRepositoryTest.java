package com.todo.repository;

import com.todo.entity.Todo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.todo.config.QueryDslConfig;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QueryDslConfig.class)
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Test
    @DisplayName("할 일을 저장하고 조회한다")
    void saveAndFind() {
        // given
        Todo todo = Todo.builder()
                .text("Test Todo")
                .completed(false)
                .displayOrder(0)
                .build();

        // when
        Todo savedTodo = todoRepository.save(todo);

        // then
        assertThat(savedTodo.getId()).isNotNull();
        assertThat(savedTodo.getText()).isEqualTo("Test Todo");
        assertThat(savedTodo.getCompleted()).isFalse();
    }

    @Test
    @DisplayName("완료된 할 일을 모두 삭제한다")
    void deleteAllCompleted() {
        // given
        Todo todo1 = Todo.builder()
                .text("Completed Todo")
                .completed(true)
                .build();
        todoRepository.save(todo1);

        Todo todo2 = Todo.builder()
                .text("Active Todo")
                .completed(false)
                .build();
        todoRepository.save(todo2);

        // when
        todoRepository.deleteCompleted();

        // then
        List<Todo> remaining = todoRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getText()).isEqualTo("Active Todo");
    }

    @Test
    @DisplayName("완료 여부에 따라 정렬하여 조회한다")
    void findByCompletedOrderByDisplayOrderAsc() {
        // given
        Todo todo1 = Todo.builder()
                .text("Active 1")
                .completed(false)
                .displayOrder(1)
                .build();
        todoRepository.save(todo1);

        Todo todo2 = Todo.builder()
                .text("Active 2")
                .completed(false)
                .displayOrder(0) // Should be first
                .build();
        todoRepository.save(todo2);

        // when
        List<Todo> activeTodos = todoRepository.findCompletedSorted(false);

        // then
        assertThat(activeTodos).hasSize(2);
        assertThat(activeTodos.get(0).getText()).isEqualTo("Active 2");
        assertThat(activeTodos.get(1).getText()).isEqualTo("Active 1");
    }
}
