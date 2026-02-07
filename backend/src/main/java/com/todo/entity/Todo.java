package com.todo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Todo 엔티티 (DB 테이블과 매핑)
 * 
 * @Entity: JPA가 관리하는 엔티티 클래스임을 표시
 * @Table: 매핑할 테이블 이름 지정
 * @Getter: Lombok - 모든 필드의 getter 자동 생성
 * @NoArgsConstructor: Lombok - 기본 생성자 자동 생성 (JPA 필수)
 */
@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {

    /**
     * 기본 키 (Primary Key)
     * 
     * @GeneratedValue: 자동 증가 (PostgreSQL SERIAL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 할 일 내용
     * 
     * @Column: 컬럼 속성 지정 (not null, 최대 500자)
     */
    @Column(nullable = false, length = 500)
    private String text;

    /**
     * 완료 여부
     */
    @Column(nullable = false)
    private Boolean completed = false;

    /**
     * 정렬 순서 (드래그앤드롭용)
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    /**
     * 생성 시간
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 빌더 패턴으로 객체 생성
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 빌더 패턴으로 객체 생성
     */
    @Builder
    public Todo(String text, Boolean completed, Integer displayOrder, Member member) {
        this.text = text;
        this.completed = completed != null ? completed : false;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.member = member;
    }

    /**
     * 엔티티 저장 전 자동 실행 (생성 시간 설정)
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 엔티티 수정 전 자동 실행 (수정 시간 갱신)
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === 비즈니스 메서드 (Setter 대신 의미있는 메서드 사용) ===

    /**
     * 할 일 내용 수정
     */
    public void updateText(String text) {
        this.text = text;
    }

    /**
     * 완료 상태 토글
     */
    public void toggleCompleted() {
        this.completed = !this.completed;
    }

    /**
     * 완료 상태 변경
     */
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    /**
     * 정렬 순서 변경
     */
    public void updateOrder(Integer order) {
        this.displayOrder = order;
    }
}
