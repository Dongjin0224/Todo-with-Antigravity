package com.todo.controller;

import com.todo.dto.TodoRequest;
import com.todo.dto.TodoResponse;
import com.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Todo REST API 컨트롤러
 * 
 * @RestController = @Controller + @ResponseBody
 * - 모든 메서드의 반환값이 JSON으로 직렬화됨
 * 
 * @RequestMapping: 기본 URL 경로 지정
 * @CrossOrigin: CORS 허용 (프론트엔드에서 호출 가능)
 */
@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*")  // 개발용 전체 허용 (운영 시 특정 도메인만)
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /**
     * 전체 Todo 조회
     * GET /api/todos
     * GET /api/todos?filter=active
     * GET /api/todos?filter=completed
     */
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAll(
            @RequestParam(required = false, defaultValue = "all") String filter) {
        List<TodoResponse> todos = todoService.findByFilter(filter);
        return ResponseEntity.ok(todos);
    }

    /**
     * 단일 Todo 조회
     * GET /api/todos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getById(@PathVariable Long id) {
        TodoResponse todo = todoService.findById(id);
        return ResponseEntity.ok(todo);
    }

    /**
     * Todo 생성
     * POST /api/todos
     * Body: { "text": "할 일 내용" }
     * 
     * @Valid: TodoRequest의 유효성 검증 실행
     * @RequestBody: JSON → 객체 변환
     */
    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoRequest request) {
        TodoResponse created = todoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Todo 수정
     * PUT /api/todos/{id}
     * Body: { "text": "수정된 내용", "completed": true }
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TodoRequest request) {
        TodoResponse updated = todoService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Todo 완료 상태 토글
     * PATCH /api/todos/{id}/toggle
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TodoResponse> toggleComplete(@PathVariable Long id) {
        TodoResponse toggled = todoService.toggleComplete(id);
        return ResponseEntity.ok(toggled);
    }

    /**
     * Todo 삭제
     * DELETE /api/todos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();  // 204 No Content
    }

    /**
     * 완료된 Todo 일괄 삭제
     * DELETE /api/todos/completed
     */
    @DeleteMapping("/completed")
    public ResponseEntity<Void> deleteCompleted() {
        todoService.deleteCompleted();
        return ResponseEntity.noContent().build();
    }

    /**
     * 통계 조회
     * GET /api/todos/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<TodoService.TodoStats> getStats() {
        return ResponseEntity.ok(todoService.getStats());
    }
}
