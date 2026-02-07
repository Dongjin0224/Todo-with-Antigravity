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
     * 완료된 항목 수 조회
     */
    long countByMemberIdAndCompleted(Long memberId, Boolean completed);

    /**
     * 전체 개수 조회 (순서 지정을 위해)
     */
    long countByMemberId(Long memberId);

    // Id로 조회 시에도 본인 것인지 확인 필요 (Service에서 처리하거나 여기서 Optional
    // findByIdAndMemberId(Long id, Long memberId) 추가 가능)
    boolean existsByIdAndMemberId(Long id, Long memberId);
}
