package com.todo.service;

import com.todo.config.JwtTokenProvider;
import com.todo.dto.AuthRequest;
import com.todo.dto.AuthResponse;
import com.todo.entity.Member;
import com.todo.entity.RefreshToken;
import com.todo.repository.MemberRepository;
import com.todo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private Authentication authentication;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("TestUser")
                .role(Member.Role.USER)
                .build();
        // Set id using reflection (since id is auto-generated)
        ReflectionTestUtils.setField(testMember, "id", 1L);
    }

    @Test
    @DisplayName("로그인 시 Access Token과 Refresh Token이 발급되어야 한다")
    void login_ShouldReturnBothTokens() {
        // given
        AuthRequest request = new AuthRequest("test@example.com", "password123", null);

        given(authenticationManagerBuilder.getObject()).willReturn(authenticationManager);
        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtTokenProvider.generateAccessToken(authentication)).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenValidity()).willReturn(1209600000L);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));

        // when
        AuthResponse response = authService.login(request);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 새로운 토큰을 재발급해야 한다")
    void reissue_WithValidRefreshToken_ShouldReturnNewTokens() {
        // given
        String oldRefreshToken = "old-refresh-token";
        RefreshToken savedToken = RefreshToken.builder()
                .id("1")
                .refreshToken(oldRefreshToken)
                .expiration(1209600000L)
                .build();

        given(refreshTokenRepository.findByRefreshToken(oldRefreshToken)).willReturn(Optional.of(savedToken));
        given(memberRepository.findById(1L)).willReturn(Optional.of(testMember));
        given(jwtTokenProvider.generateAccessToken(any())).willReturn("new-access-token");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("new-refresh-token");
        given(jwtTokenProvider.getRefreshTokenValidity()).willReturn(1209600000L);

        // when
        AuthResponse response = authService.reissue(oldRefreshToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 예외 발생")
    void reissue_WithInvalidRefreshToken_ShouldThrowException() {
        // given
        String invalidToken = "invalid-token";
        given(refreshTokenRepository.findByRefreshToken(invalidToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(invalidToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("유효하지 않은 Refresh Token입니다.");
    }

    @Test
    @DisplayName("로그아웃 시 Refresh Token이 삭제되어야 한다")
    void logout_ShouldDeleteRefreshToken() {
        // given
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));

        // when
        authService.logout("test@example.com");

        // then
        verify(refreshTokenRepository).deleteById("1");
    }
}
