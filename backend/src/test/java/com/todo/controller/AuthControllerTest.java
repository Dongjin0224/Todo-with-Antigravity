package com.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.dto.AuthRequest;
import com.todo.dto.AuthResponse;
import com.todo.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 시 Access Token과 Refresh Token을 반환해야 한다")
    void login_Success_ShouldReturnTokens() throws Exception {
        // given
        AuthRequest request = new AuthRequest("test@example.com", "password123", null);

        AuthResponse response = AuthResponse.builder()
                .grantType("Bearer")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .email("test@example.com")
                .nickname("TestUser")
                .role("USER")
                .build();

        given(authService.login(any(AuthRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("Refresh Token 재발급 성공")
    void reissue_Success_ShouldReturnNewTokens() throws Exception {
        // given
        Map<String, String> request = Map.of("refreshToken", "old-refresh-token");

        AuthResponse response = AuthResponse.builder()
                .grantType("Bearer")
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .email("test@example.com")
                .nickname("TestUser")
                .role("USER")
                .build();

        given(authService.reissue("old-refresh-token")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("로그아웃 성공")
    @WithMockUser(username = "test@example.com")
    void logout_Success() throws Exception {
        // given
        doNothing().when(authService).logout("test@example.com");

        // when & then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃 성공"));
    }
}
