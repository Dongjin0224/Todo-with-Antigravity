package com.todo.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("유효성 검증 실패 시 400과 errors 필드를 반환한다")
    void validationError_ShouldReturn400WithErrors() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValidateRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.text").exists());
    }

    @Test
    @DisplayName("AuthenticationException 발생 시 401을 반환한다")
    void authenticationException_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/test/auth-fail"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증에 실패했습니다."));
    }

    @Test
    @DisplayName("처리되지 않은 예외는 500을 반환한다")
    void unhandledException_ShouldReturn500() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다"));
    }

    @RestController
    static class TestController {
        @PostMapping("/test/validate")
        public String validate(@Valid @RequestBody ValidateRequest request) {
            return "ok";
        }

        @GetMapping("/test/auth-fail")
        public String authFail() {
            throw new BadCredentialsException("bad credentials");
        }

        @GetMapping("/test/error")
        public String error() {
            throw new RuntimeException("unexpected");
        }
    }

    record ValidateRequest(@NotBlank String text) {
    }
}
