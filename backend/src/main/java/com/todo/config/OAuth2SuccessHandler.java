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
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // 이메일 추출 (Google vs Kakao)
            String email = extractEmail(oAuth2User);
            if (email == null || email.isBlank()) {
                redirectWithError(request, response, "이메일 정보를 가져올 수 없습니다.");
                return;
            }

            log.info("OAuth2 로그인 성공: email={}", email);

            // 회원 조회
            Optional<Member> memberOptional = memberRepository.findByEmail(email);
            if (memberOptional.isEmpty()) {
                redirectWithError(request, response, "회원 정보를 찾을 수 없습니다.");
                return;
            }
            Member member = memberOptional.get();

            // JWT 토큰 생성 (nickname 포함)
            String accessToken = jwtTokenProvider.generateAccessToken(authentication, member.getNickname());
            String refreshToken = jwtTokenProvider.generateRefreshToken();

            // RefreshToken 저장 (기존 토큰이 있으면 갱신)
            saveOrUpdateRefreshToken(member.getId().toString(), refreshToken);

            // 프론트엔드로 리다이렉트 (토큰 포함)
            String targetUrl = UriComponentsBuilder.fromUriString(appProperties.getAuthorizedRedirectUri())
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생", e);
            redirectWithError(request, response, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private void saveOrUpdateRefreshToken(String memberId, String refreshToken) {
        // 기존 토큰 삭제 후 새로 저장 (Redis는 자동으로 덮어쓰기됨)
        refreshTokenRepository.deleteById(memberId);

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
}
