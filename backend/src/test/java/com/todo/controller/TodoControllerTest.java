package com.todo.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.config.RestDocsConfig;
import com.todo.config.CorsProperties;
import com.todo.dto.TodoRequest;
import com.todo.dto.TodoResponse;
import com.todo.exception.ForbiddenException;
import com.todo.exception.ResourceNotFoundException;
import com.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.todo.config.TestSecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TodoController.class)
@Import({ com.todo.config.SecurityConfig.class, RestDocsConfig.class, TestSecurityConfig.class })
@AutoConfigureRestDocs
class TodoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private TodoService todoService;

        @MockBean
        private com.todo.config.JwtTokenProvider jwtTokenProvider;

        @MockBean
        private com.todo.exception.CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @MockBean
        private com.todo.exception.CustomAccessDeniedHandler customAccessDeniedHandler;

        @MockBean
        private CorsProperties corsProperties;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUpCorsProperties() {
                given(corsProperties.getAllowedOrigins()).willReturn(List.of("http://localhost:3000"));
                given(corsProperties.getAllowedMethods())
                                .willReturn(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                given(corsProperties.getAllowedHeaders()).willReturn(List.of("*"));
                given(corsProperties.isAllowCredentials()).willReturn(false);
                given(corsProperties.getMaxAge()).willReturn(3600L);
        }

        @Test
        @DisplayName("할 일 목록을 조회한다")
        @WithMockUser
        void getAll() throws Exception {
                // given
                TodoResponse todo1 = new TodoResponse(1L, "Test 1", false, 0, null, null);
                TodoResponse todo2 = new TodoResponse(2L, "Test 2", true, 1, null, null);
                given(todoService.findByFilter(anyString())).willReturn(List.of(todo1, todo2));

                // when & then
                mockMvc.perform(get("/api/todos")
                                .param("filter", "all"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].text").value("Test 1"))
                                .andDo(MockMvcRestDocumentationWrapper.document("todo-get-all",
                                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                                                .tag("Todo")
                                                                .summary("할 일 목록 조회")
                                                                .description("필터 조건에 따라 할 일 목록을 조회합니다.")
                                                                .queryParameters(
                                                                                parameterWithName("filter").description(
                                                                                                "필터 조건 (all, active, completed)")
                                                                                                .optional())
                                                                .responseFields(
                                                                                fieldWithPath("[].id").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("할 일 ID"),
                                                                                fieldWithPath("[].text").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("할 일 내용"),
                                                                                fieldWithPath("[].completed").type(
                                                                                                JsonFieldType.BOOLEAN)
                                                                                                .description("완료 여부"),
                                                                                fieldWithPath("[].displayOrder").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("정렬 순서"),
                                                                                fieldWithPath("[].createdAt").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("생성일시")
                                                                                                .optional(),
                                                                                fieldWithPath("[].updatedAt").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("수정일시")
                                                                                                .optional())
                                                                .build())));
        }

        @Test
        @DisplayName("새로운 할 일을 생성한다")
        @WithMockUser
        void create() throws Exception {
                // given
                TodoRequest request = new TodoRequest("New Todo", null, null);
                TodoResponse response = new TodoResponse(1L, "New Todo", false, 0, null, null);
                given(todoService.create(any(TodoRequest.class))).willReturn(response);

                // when & then
                mockMvc.perform(post("/api/todos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                                .andDo(print())
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.text").value("New Todo"))
                                .andDo(MockMvcRestDocumentationWrapper.document("todo-create",
                                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                                                .tag("Todo")
                                                                .summary("할 일 생성")
                                                                .description("새로운 할 일을 생성합니다.")
                                                                .requestFields(
                                                                                fieldWithPath("text").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("할 일 내용"),
                                                                                fieldWithPath("completed").type(
                                                                                                JsonFieldType.BOOLEAN)
                                                                                                .description("완료 여부")
                                                                                                .optional(),
                                                                                fieldWithPath("displayOrder").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("정렬 순서")
                                                                                                .optional())
                                                                .responseFields(
                                                                                fieldWithPath("id").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("할 일 ID"),
                                                                                fieldWithPath("text").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("할 일 내용"),
                                                                                fieldWithPath("completed").type(
                                                                                                JsonFieldType.BOOLEAN)
                                                                                                .description("완료 여부"),
                                                                                fieldWithPath("displayOrder").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("정렬 순서"),
                                                                                fieldWithPath("createdAt").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("생성일시")
                                                                                                .optional(),
                                                                                fieldWithPath("updatedAt").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("수정일시")
                                                                                                .optional())
                                                                .build())));
        }

        @Test
        @DisplayName("통계를 조회한다")
        @WithMockUser
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
                                .andExpect(jsonPath("$.completed").value(5L))
                                .andDo(MockMvcRestDocumentationWrapper.document("todo-get-stats",
                                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                                                .tag("Todo")
                                                                .summary("통계 조회")
                                                                .description("할 일 통계를 조회합니다.")
                                                                .responseFields(
                                                                                fieldWithPath("total").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("전체 할 일 수"),
                                                                                fieldWithPath("active").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("진행 중인 할 일 수"),
                                                                                fieldWithPath("completed").type(
                                                                                                JsonFieldType.NUMBER)
                                                                                                .description("완료된 할 일 수"))
                                                                .build())));
        }

        @Test
        @DisplayName("존재하지 않는 Todo 조회 시 404를 반환한다")
        @WithMockUser
        void getById_NotFound_ShouldReturn404() throws Exception {
                given(todoService.findById(999L))
                                .willThrow(new ResourceNotFoundException("Todo not found: 999"));

                mockMvc.perform(get("/api/todos/999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").value("Todo not found: 999"));
        }

        @Test
        @DisplayName("권한 없는 Todo 조회 시 403을 반환한다")
        @WithMockUser
        void getById_Forbidden_ShouldReturn403() throws Exception {
                given(todoService.findById(10L))
                                .willThrow(new ForbiddenException("해당 Todo에 대한 권한이 없습니다."));

                mockMvc.perform(get("/api/todos/10"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.status").value(403))
                                .andExpect(jsonPath("$.message").value("해당 Todo에 대한 권한이 없습니다."));
        }

        @Test
        @DisplayName("존재하지 않는 Todo 수정 시 404를 반환한다")
        @WithMockUser
        void update_NotFound_ShouldReturn404() throws Exception {
                TodoRequest request = new TodoRequest("Updated Todo", false, 0);
                given(todoService.update(org.mockito.ArgumentMatchers.eq(404L), any(TodoRequest.class)))
                                .willThrow(new ResourceNotFoundException("Todo not found: 404"));

                mockMvc.perform(put("/api/todos/404")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404));
        }
}
