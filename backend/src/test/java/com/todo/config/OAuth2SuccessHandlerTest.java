package com.todo.config;

import com.todo.entity.Member;
import com.todo.entity.RefreshToken;
import com.todo.repository.MemberRepository;
import com.todo.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AppProperties appProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@example.com")
                .nickname("TestUser")
                .role(Member.Role.USER)
                .provider(Member.Provider.GOOGLE)
                .providerId("google-123")
                .build();

        // Set ID via reflection
        try {
            var idField = Member.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testMember, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 시 토큰과 함께 리다이렉트")
    void onAuthenticationSuccess_ValidUser_RedirectsWithTokens() throws Exception {
        // given
        given(authentication.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getAttributes()).willReturn(Map.of("email", "test@example.com"));
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));
        given(jwtTokenProvider.generateAccessToken(any(), anyString())).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenValidity()).willReturn(1209600000L);
        given(appProperties.getAuthorizedRedirectUri()).willReturn("http://localhost:3000/auth/oauth/callback");

        oAuth2SuccessHandler.setRedirectStrategy(redirectStrategy);

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(any(), any(), urlCaptor.capture());

        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).contains("accessToken=access-token");
        assertThat(redirectUrl).contains("refreshToken=refresh-token");
    }

    @Test
    @DisplayName("이메일이 없는 경우 에러 리다이렉트")
    void onAuthenticationSuccess_NoEmail_RedirectsWithError() throws Exception {
        // given
        given(authentication.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getAttributes()).willReturn(Map.of("sub", "google-id"));
        given(appProperties.getAuthorizedRedirectUri()).willReturn("http://localhost:3000/auth/oauth/callback");

        oAuth2SuccessHandler.setRedirectStrategy(redirectStrategy);

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(any(), any(), urlCaptor.capture());

        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).contains("error=");
    }

    @Test
    @DisplayName("회원이 존재하지 않는 경우 에러 리다이렉트")
    void onAuthenticationSuccess_MemberNotFound_RedirectsWithError() throws Exception {
        // given
        given(authentication.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getAttributes()).willReturn(Map.of("email", "nonexistent@example.com"));
        given(memberRepository.findByEmail("nonexistent@example.com")).willReturn(Optional.empty());
        given(appProperties.getAuthorizedRedirectUri()).willReturn("http://localhost:3000/auth/oauth/callback");

        oAuth2SuccessHandler.setRedirectStrategy(redirectStrategy);

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(redirectStrategy).sendRedirect(any(), any(), urlCaptor.capture());

        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).contains("error=");
    }

    @Test
    @DisplayName("RefreshToken이 저장되어야 한다")
    void onAuthenticationSuccess_SavesRefreshToken() throws Exception {
        // given
        given(authentication.getPrincipal()).willReturn(oAuth2User);
        given(oAuth2User.getAttributes()).willReturn(Map.of("email", "test@example.com"));
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(testMember));
        given(jwtTokenProvider.generateAccessToken(any(), anyString())).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("refresh-token");
        given(jwtTokenProvider.getRefreshTokenValidity()).willReturn(1209600000L);
        given(appProperties.getAuthorizedRedirectUri()).willReturn("http://localhost:3000/auth/oauth/callback");

        oAuth2SuccessHandler.setRedirectStrategy(redirectStrategy);

        // when
        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getRefreshToken()).isEqualTo("refresh-token");
    }
}
