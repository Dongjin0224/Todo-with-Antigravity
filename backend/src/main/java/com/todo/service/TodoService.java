package com.todo.service;

import com.todo.dto.TodoRequest;
import com.todo.dto.TodoResponse;
import com.todo.entity.Todo;
import com.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Todo 서비스 (비즈니스 로직 계층)
 * 
 * @Service: 스프링 빈으로 등록 (비즈니스 로직 담당)
 * @RequiredArgsConstructor: final 필드 생성자 자동 생성 (DI)
 * @Transactional: 트랜잭션 관리 (예외 발생 시 롤백)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 (성능 최적화)
public class TodoService {

    private final TodoRepository todoRepository;

    /**
     * 전체 Todo 조회
     */
    public List<TodoResponse> findAll() {
        return todoRepository.findAllSorted()
                .stream()
                .map(TodoResponse::from) // Entity → DTO 변환
                .collect(Collectors.toList());
    }

    /**
     * 필터별 Todo 조회
     */
    public List<TodoResponse> findByFilter(String filter) {
        List<Todo> todos;

        switch (filter) {
            case "active":
                todos = todoRepository.findCompletedSorted(false);
                break;
            case "completed":
                todos = todoRepository.findCompletedSorted(true);
                break;
            default:
                todos = todoRepository.findAllSorted();
        }

        return todos.stream()
                .map(TodoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 단일 Todo 조회
     */
    public TodoResponse findById(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found: " + id));
        return TodoResponse.from(todo);
    }

    /**
     * Todo 생성
     * 
     * @Transactional: 쓰기 작업이므로 readOnly = false
     */
    @Transactional
    public TodoResponse create(TodoRequest request) {
        // 새 Todo의 순서는 현재 개수 (맨 뒤에 추가)
        int order = (int) todoRepository.count();

        Todo todo = Todo.builder()
                .text(request.getText())
                .completed(request.getCompleted())
                .displayOrder(order)
                .build();

        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    /**
     * Todo 수정
     */
    @Transactional
    public TodoResponse update(Long id, TodoRequest request) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found: " + id));

        // 변경할 필드만 업데이트
        if (request.getText() != null) {
            todo.updateText(request.getText());
        }
        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }
        if (request.getDisplayOrder() != null) {
            todo.updateOrder(request.getDisplayOrder());
        }

        // JPA 변경 감지 (Dirty Checking) - save() 호출 안 해도 자동 UPDATE
        return TodoResponse.from(todo);
    }

    /**
     * Todo 완료 상태 토글
     */
    @Transactional
    public TodoResponse toggleComplete(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found: " + id));

        todo.toggleCompleted();
        return TodoResponse.from(todo);
    }

    /**
     * Todo 삭제
     */
    @Transactional
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new IllegalArgumentException("Todo not found: " + id);
        }
        todoRepository.deleteById(id);
    }

    /**
     * 완료된 Todo 일괄 삭제
     */
    @Transactional
    public void deleteCompleted() {
        todoRepository.deleteCompleted();
    }

    /**
     * 통계 조회 (전체, 진행중, 완료)
     */
    public TodoStats getStats() {
        long total = todoRepository.count();
        long completed = todoRepository.countByCompleted(true);
        long active = total - completed;

        return new TodoStats(total, active, completed);
    }

    /**
     * 통계 데이터 클래스
     */
    public record TodoStats(long total, long active, long completed) {
    }
}
