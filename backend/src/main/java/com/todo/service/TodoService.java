package com.todo.service;

import com.todo.dto.TodoRequest;
import com.todo.dto.TodoResponse;
import com.todo.entity.Member;
import com.todo.entity.Todo;
import com.todo.exception.ForbiddenException;
import com.todo.exception.ResourceNotFoundException;
import com.todo.exception.UnauthorizedException;
import com.todo.repository.MemberRepository;
import com.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;//

import java.util.List;
import java.util.stream.Collectors;

/**
 * Todo 서비스 (비즈니스 로직 계층)
 * 
 * @Service: 스프링 빈으로 등록 (비즈니스 로직 담당)
 * @RequiredArgsConstructor: final 필드 생성자 자동 생성 (DI)
 * @Transactional: 트랜잭션 관리 (예외 발생 시 롤백)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final MemberRepository memberRepository;

    /**
     * 전체 Todo 조회 (현재 로그인한 사용자 기준)
     */
    public List<TodoResponse> findAll() {
        Member currentMember = getCurrentMember();
        return todoRepository.findAllSorted(currentMember.getId())
                .stream()
                .map(TodoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 필터별 Todo 조회 (현재 로그인한 사용자 기준)
     */
    public List<TodoResponse> findByFilter(String filter) {
        Member currentMember = getCurrentMember();
        List<Todo> todos;

        switch (filter) {
            case "active":
                todos = todoRepository.findCompletedSorted(currentMember.getId(), false);
                break;
            case "completed":
                todos = todoRepository.findCompletedSorted(currentMember.getId(), true);
                break;
            default:
                todos = todoRepository.findAllSorted(currentMember.getId());
        }

        return todos.stream()
                .map(TodoResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 단일 Todo 조회 (본인 것만 허용)
     */
    public TodoResponse findById(Long id) {
        Member currentMember = getCurrentMember();
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));

        validateOwnership(todo, currentMember);
        return TodoResponse.from(todo);
    }

    /**
     * Todo 생성
     */
    @Transactional
    public TodoResponse create(TodoRequest request) {
        Member currentMember = getCurrentMember();

        // 새 Todo의 순서는 현재 개수 (맨 뒤에 추가)
        int order = (int) todoRepository.countByMemberId(currentMember.getId());

        Todo todo = Todo.builder()
                .text(request.getText())
                .completed(request.getCompleted())
                .displayOrder(order)
                .member(currentMember)
                .build();

        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    /**
     * Todo 수정
     */
    @Transactional
    public TodoResponse update(Long id, TodoRequest request) {
        Member currentMember = getCurrentMember();
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));

        validateOwnership(todo, currentMember);

        if (request.getText() != null) {
            todo.updateText(request.getText());
        }
        if (request.getCompleted() != null) {
            todo.setCompleted(request.getCompleted());
        }
        if (request.getDisplayOrder() != null) {
            todo.updateOrder(request.getDisplayOrder());
        }

        return TodoResponse.from(todo);
    }

    /**
     * Todo 완료 상태 토글
     */
    @Transactional
    public TodoResponse toggleComplete(Long id) {
        Member currentMember = getCurrentMember();
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));

        validateOwnership(todo, currentMember);

        todo.toggleCompleted();
        return TodoResponse.from(todo);
    }

    /**
     * Todo 삭제
     */
    @Transactional
    public void delete(Long id) {
        Member currentMember = getCurrentMember();
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));

        validateOwnership(todo, currentMember);

        todoRepository.deleteById(id);
    }

    /**
     * 완료된 Todo 일괄 삭제 (본인 것만)
     */
    @Transactional
    public void deleteCompleted() {
        Member currentMember = getCurrentMember();
        todoRepository.deleteCompleted(currentMember.getId());
    }

    /**
     * 통계 조회
     */
    public TodoStats getStats() {
        Member currentMember = getCurrentMember();
        long total = todoRepository.countByMemberId(currentMember.getId());
        long completed = todoRepository.countByMemberIdAndCompleted(currentMember.getId(), true);
        long active = total - completed;

        return new TodoStats(total, active, completed);
    }

    /**
     * 현재 로그인한 사용자 가져오기
     */
    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("Authentication object is null");
            throw new UnauthorizedException("로그인된 사용자를 찾을 수 없습니다. (Auth is null)");
        }
        String email = authentication.getName();
        log.info("getCurrentMember email: {}", email);
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("로그인된 사용자를 찾을 수 없습니다."));
    }

    /**
     * 소유권 검증 (내 Todo가 맞는지)
     */
    private void validateOwnership(Todo todo, Member member) {
        if (!todo.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException("해당 Todo에 대한 권한이 없습니다.");
        }
    }

    public record TodoStats(long total, long active, long completed) {
    }
}
