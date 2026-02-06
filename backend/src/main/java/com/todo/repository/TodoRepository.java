package com.todo.repository;

import com.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Todo Repository (데이터 접근 계층)
 * 
 * QueryDSL 적용:
 * - JpaRepository: 기본 CRUD 제공
 * - TodoRepositoryCustom: 복잡한 조회 및 벌크 연산 제공 (QueryDSL 구현)
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    /**
     * 완료된 항목 수 조회 (간단하므로 유지)
     */
    long countByCompleted(Boolean completed);

    // 기존의 복잡한 메서드들은 TodoRepositoryCustom으로 대체됨
    // - findAllByOrderByDisplayOrderAscCreatedAtDesc -> findAllSorted()
    // - findByCompletedOrderByDisplayOrderAsc -> findCompletedSorted()
    // - deleteAllCompleted -> deleteCompleted()
}
