package com.todo.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.config.RestDocsConfig;
import com.todo.dto.AuthRequest;
import com.todo.dto.AuthResponse;
import com.todo.exception.DuplicateResourceException;
import com.todo.exception.UnauthorizedException;
import com.todo.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import com.todo.config.TestSecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import({ RestDocsConfig.class, TestSecurityConfig.class })
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
                                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                                .andDo(MockMvcRestDocumentationWrapper.document("auth-login",
                                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                                                .tag("Auth")
                                                                .summary("로그인")
                                                                .description("이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
                                                                .requestFields(
                                                                                fieldWithPath("email").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("이메일"),
                                                                                fieldWithPath("password").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("비밀번호"))
                                                                .responseFields(
                                                                                fieldWithPath("grantType").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("토큰 타입"),
                                                                                fieldWithPath("accessToken").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("액세스 토큰"),
                                                                                fieldWithPath("refreshToken").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("리프레시 토큰"),
                                                                                fieldWithPath("email").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("이메일"),
                                                                                fieldWithPath("nickname").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("닉네임"),
                                                                                fieldWithPath("role").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("사용자 역할"))
                                                                .build())));
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
                                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                                .andDo(MockMvcRestDocumentationWrapper.document("auth-reissue",
                                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                                                .tag("Auth")
                                                                .summary("토큰 재발급")
                                                                .description("리프레시 토큰으로 새로운 토큰을 발급받습니다.")
                                                                .requestFields(
                                                                                fieldWithPath("refreshToken").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("리프레시 토큰"))
                                                                .responseFields(
                                                                                fieldWithPath("grantType").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("토큰 타입"),
                                                                                fieldWithPath("accessToken").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("새 액세스 토큰"),
                                                                                fieldWithPath("refreshToken").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("새 리프레시 토큰"),
                                                                                fieldWithPath("email").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("이메일"),
                                                                                fieldWithPath("nickname").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("닉네임"),
                                                                                fieldWithPath("role").type(
                                                                                                JsonFieldType.STRING)
                                                                                                .description("사용자 역할"))
                                                                .build())));
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
                                .andExpect(content().string("로그아웃 성공"))
                                .andDo(MockMvcRestDocumentationWrapper.document("auth-logout",
                                                ResourceDocumentation.resource(ResourceSnippetParameters.builder()
                                                                .tag("Auth")
                                                                .summary("로그아웃")
                                                                .description("현재 사용자를 로그아웃합니다.")
                                                                .build())));
        }

        @Test
        @DisplayName("로그아웃 요청 시 비인증 사용자는 401을 반환한다")
        void logout_Unauthenticated_ShouldReturn401() throws Exception {
                mockMvc.perform(post("/api/auth/logout"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("회원가입 시 중복 유저면 409를 반환한다")
        void signup_DuplicateUser_ShouldReturn409() throws Exception {
                AuthRequest request = new AuthRequest("test@example.com", "password123", "tester");
                doThrow(new DuplicateResourceException("이미 가입되어 있는 유저입니다."))
                                .when(authService).signup(any(AuthRequest.class));

                mockMvc.perform(post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.message").value("이미 가입되어 있는 유저입니다."));
        }

        @Test
        @DisplayName("토큰 재발급 시 리프레시 토큰이 유효하지 않으면 401을 반환한다")
        void reissue_InvalidRefreshToken_ShouldReturn401() throws Exception {
                Map<String, String> request = Map.of("refreshToken", "invalid-token");
                given(authService.reissue("invalid-token"))
                                .willThrow(new UnauthorizedException("유효하지 않은 Refresh Token입니다."));

                mockMvc.perform(post("/api/auth/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.message").value("유효하지 않은 Refresh Token입니다."));
        }
}
