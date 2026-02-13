package com.todo.config;

import com.todo.entity.Member;
import com.todo.repository.MemberRepository;
import com.todo.entity.RefreshToken;
import com.todo.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User;
        try {
            oAuth2User = (OAuth2User) authentication.getPrincipal();
        } catch (ClassCastException e) {
            log.error("OAuth2User 캐스팅 실패", e);
            redirectWithError(request, response, "인증 정보 처리 중 오류가 발생했습니다.");
            return;
        }

        // 이메일 추출 (Google vs Kakao)
        String email;
        try {
            email = extractEmail(oAuth2User);
        } catch (Exception e) {
            log.error("이메일 추출 중 오류 발생", e);
            redirectWithError(request, response, "이메일 정보를 가져올 수 없습니다.");
            return;
        }

        if (email == null || email.isBlank()) {
            log.warn("OAuth 응답에 이메일이 없습니다");
            redirectWithError(request, response, "이메일 정보를 가져올 수 없습니다.");
            return;
        }

        log.info("OAuth2 로그인 성공: email={}", maskEmail(email));

        // 회원 조회
        Optional<Member> memberOptional;
        try {
            memberOptional = memberRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("회원 조회 중 오류 발생: email={}", maskEmail(email), e);
            redirectWithError(request, response, "회원 정보 조회 중 오류가 발생했습니다.");
            return;
        }

        if (memberOptional.isEmpty()) {
            log.warn("OAuth 로그인 실패 - 회원 없음: email={}", maskEmail(email));
            redirectWithError(request, response, "회원 정보를 찾을 수 없습니다.");
            return;
        }
        Member member = memberOptional.get();

        // JWT 토큰 생성 (nickname 포함)
        String accessToken;
        String refreshToken;
        try {
            accessToken = jwtTokenProvider.generateAccessToken(authentication, member.getNickname());
            refreshToken = jwtTokenProvider.generateRefreshToken();
        } catch (Exception e) {
            log.error("JWT 토큰 생성 중 오류 발생", e);
            redirectWithError(request, response, "토큰 생성 중 오류가 발생했습니다.");
            return;
        }

        // RefreshToken 저장 (덮어쓰기 방식)
        try {
            saveRefreshToken(member.getId().toString(), refreshToken);
        } catch (Exception e) {
            log.error("RefreshToken 저장 중 오류 발생", e);
            redirectWithError(request, response, "토큰 저장 중 오류가 발생했습니다.");
            return;
        }

        // 프론트엔드로 리다이렉트 (토큰 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(appProperties.getAuthorizedRedirectUri())
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * RefreshToken 저장 (Redis는 ID로 덮어쓰기됨)
     */
    private void saveRefreshToken(String memberId, String refreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(memberId)
                .refreshToken(refreshToken)
                .expiration(jwtTokenProvider.getRefreshTokenValidity())
                .build();
        refreshTokenRepository.save(refreshTokenEntity);
        log.debug("RefreshToken 저장 완료: memberId={}", memberId);
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws IOException {
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        String errorUrl = UriComponentsBuilder.fromUriString(appProperties.getAuthorizedRedirectUri())
                .queryParam("error", encodedMessage)
                .build().toUriString();
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }

    private String extractEmail(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Google
        if (attributes.containsKey("email")) {
            return (String) attributes.get("email");
        }

        // Kakao
        if (attributes.containsKey("kakao_account")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.containsKey("email")) {
                return (String) kakaoAccount.get("email");
            }
        }

        log.warn("OAuth 응답에서 이메일을 찾을 수 없습니다: {}", attributes.keySet());
        return null;
    }

    /**
     * 이메일 마스킹 (예: test@example.com → t***@example.com)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) {
            return "***" + email.substring(atIndex);
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
