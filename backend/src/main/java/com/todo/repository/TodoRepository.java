package com.todo.repository;

import com.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Todo Repository (데이터 접근 계층)
 * 
 * JpaRepository를 상속받으면 기본 CRUD 메서드 자동 제공:
 * - save(entity): 저장/수정
 * - findById(id): ID로 조회
 * - findAll(): 전체 조회
 * - deleteById(id): 삭제
 * - count(): 개수 조회
 * 등등...
 * 
 * 메서드 이름으로 쿼리 자동 생성 (Query Method):
 * - findByCompleted(true) → SELECT * FROM todos WHERE completed = true
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    /**
     * 정렬 순서대로 전체 조회
     */
    List<Todo> findAllByOrderByDisplayOrderAscCreatedAtDesc();

    /**
     * 완료 여부로 필터링하여 조회
     */
    List<Todo> findByCompletedOrderByDisplayOrderAsc(Boolean completed);

    /**
     * 완료된 항목 수 조회
     */
    long countByCompleted(Boolean completed);

    /**
     * 완료된 항목 일괄 삭제
     * @Modifying: UPDATE/DELETE 쿼리임을 표시
     * @Query: 직접 JPQL/SQL 작성
     */
    @Modifying
    @Query("DELETE FROM Todo t WHERE t.completed = true")
    void deleteAllCompleted();
}
