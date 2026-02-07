package com.todo.service;

import com.todo.dto.TodoRequest;
import com.todo.dto.TodoResponse;
import com.todo.entity.Todo;
import com.todo.repository.TodoRepository;
import com.todo.entity.Member;
import com.todo.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TodoService todoService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("test@example.com")
                .password("password")
                .nickname("tester")
                .role(Member.Role.USER)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", 1L);

        // SecurityContext Mocking
        Authentication authentication = mock(Authentication.class);
        given(authentication.getName()).willReturn("test@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("새로운 할 일을 생성한다")
    void create() {
        // given
        TodoRequest request = new TodoRequest("New Todo", null, null);

        Todo todo = Todo.builder()
                .text("New Todo")
                .completed(false)
                .member(member)
                .build();
        org.springframework.test.util.ReflectionTestUtils.setField(todo, "id", 1L);

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(todoRepository.countByMemberId(member.getId())).willReturn(0L);
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
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));
        given(todoRepository.countByMemberId(member.getId())).willReturn(10L);
        given(todoRepository.countByMemberIdAndCompleted(member.getId(), true)).willReturn(3L);

        // when
        TodoService.TodoStats stats = todoService.getStats();

        // then
        assertThat(stats.total()).isEqualTo(10L);
        assertThat(stats.completed()).isEqualTo(3L);
        assertThat(stats.active()).isEqualTo(7L);
    }
}
