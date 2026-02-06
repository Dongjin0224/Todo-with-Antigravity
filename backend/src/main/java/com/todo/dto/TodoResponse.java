package com.todo.dto;

import com.todo.entity.Todo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Todo 응답 DTO
 * 
 * Entity 대신 DTO를 응답하면:
 * 1. 필요한 필드만 선택적으로 노출
 * 2. 순환 참조 문제 방지 (연관 관계 있을 때)
 * 3. API 스펙 안정성 (Entity 변경해도 API 영향 없음)
 */
@Getter
@Builder
public class TodoResponse {

    private Long id;
    private String text;
    private Boolean completed;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity → DTO 변환 정적 팩토리 메서드
     */
    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .text(todo.getText())
                .completed(todo.getCompleted())
                .displayOrder(todo.getDisplayOrder())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}
