package com.todo.service;

import com.todo.dto.TodoRequest;
import com.todo.dto.TodoResponse;
import com.todo.entity.Todo;
import com.todo.repository.TodoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("새로운 할 일을 생성한다")
    void create() {
        // given
        TodoRequest request = new TodoRequest("New Todo", null, null);

        Todo todo = Todo.builder()
                .text("New Todo")
                .completed(false)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(todo, "id", 1L);

        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when
        TodoResponse response = todoService.create(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getText()).isEqualTo("New Todo");
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("통계를 조회한다")
    void getStats() {
        // given
        given(todoRepository.count()).willReturn(10L);
        given(todoRepository.countByCompleted(true)).willReturn(3L);

        // when
        TodoService.TodoStats stats = todoService.getStats();

        // then
        assertThat(stats.total()).isEqualTo(10L);
        assertThat(stats.completed()).isEqualTo(3L);
        assertThat(stats.active()).isEqualTo(7L);
    }
}
