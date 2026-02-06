package com.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Todo 생성/수정 요청 DTO (Data Transfer Object)
 * 
 * DTO를 사용하는 이유:
 * 1. Entity를 직접 노출하지 않아 보안 향상
 * 2. API 스펙과 DB 스키마 분리 (유연성)
 * 3. 요청 데이터 검증 (@Valid)
 */
@Getter
@NoArgsConstructor
public class TodoRequest {

    /**
     * @NotBlank: null, 빈 문자열, 공백만 있는 문자열 모두 불허
     * @Size: 문자열 길이 제한
     */
    @NotBlank(message = "할 일 내용은 필수입니다")
    @Size(max = 500, message = "할 일은 500자 이내로 입력해주세요")
    private String text;

    private Boolean completed;

    private Integer displayOrder;

    public TodoRequest(String text, Boolean completed, Integer displayOrder) {
        this.text = text;
        this.completed = completed;
        this.displayOrder = displayOrder;
    }
}
