package com.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.dto.TodoRequest;
import com.todo.dto.TodoResponse;
import com.todo.service.TodoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TodoController.class)
@org.springframework.context.annotation.Import(com.todo.config.SecurityConfig.class)
// @Import(SecurityConfig.class) -> SecurityConfig invokes JwtTokenProvider
// which might not be mocked.
// Alternative: Mock Security dependencies or use @WithMockUser and exclude
// filters.
// Let's use @WithMockUser and mock JwtTokenProvider bean required by
// SecurityConfig.
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    @MockBean
    private com.todo.config.JwtTokenProvider jwtTokenProvider; // Required for SecurityConfig

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("할 일 목록을 조회한다")
    @org.springframework.security.test.context.support.WithMockUser
    void getAll() throws Exception {
        // given
        TodoResponse todo1 = new TodoResponse(1L, "Test 1", false, 0, null, null);
        TodoResponse todo2 = new TodoResponse(2L, "Test 2", true, 1, null, null);
        given(todoService.findByFilter(anyString())).willReturn(List.of(todo1, todo2));

        // when & then
        mockMvc.perform(get("/api/todos")
                .param("filter", "all")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .csrf())) // CSRF needed for POST/PUT if enabled, but disabled in config. However,
                                  // @WebMvcTest might default.
                // Our SecurityConfig disables CSRF.
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Test 1"));
    }

    @Test
    @DisplayName("새로운 할 일을 생성한다")
    @org.springframework.security.test.context.support.WithMockUser
    void create() throws Exception {
        // given
        TodoRequest request = new TodoRequest("New Todo", null, null);

        TodoResponse response = new TodoResponse(1L, "New Todo", false, 0, null, null);
        given(todoService.create(any(TodoRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("New Todo"));
    }

    @Test
    @DisplayName("통계를 조회한다")
    @org.springframework.security.test.context.support.WithMockUser
    void getStats() throws Exception {
        // given
        TodoService.TodoStats stats = new TodoService.TodoStats(10L, 5L, 5L);
        given(todoService.getStats()).willReturn(stats);

        // when & then
        mockMvc.perform(get("/api/todos/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10L))
                .andExpect(jsonPath("$.active").value(5L))
                .andExpect(jsonPath("$.completed").value(5L));
    }
}
