package com.todo.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SecretKey;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET_KEY = "VG9kb0FwcFNlY3JldEtleU11c3RCZUxvbmdFbm91Z2hGb3JIUzUxMkFsZ29yaXRobU9yYXRMZWFzdDI1NkJpdHM=";
    private static final long ACCESS_TOKEN_VALIDITY = 1800000L;
    private static final long REFRESH_TOKEN_VALIDITY = 1209600000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET_KEY, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY);
    }

    private Authentication createAuthentication(String email) {
        User principal = new User(email, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    @Test
    @DisplayName("Access Token 생성 시 nickname claim 포함")
    void generateAccessToken_WithNickname_IncludesNicknameClaim() {
        // given
        Authentication authentication = createAuthentication("test@example.com");
        String nickname = "TestUser";

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(authentication, nickname);

        // then
        assertThat(accessToken).isNotBlank();

        // Parse token and verify nickname claim
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(claims.get("nickname")).isEqualTo("TestUser");
        assertThat(claims.getSubject()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("nickname이 null인 경우 claim에 포함되지 않음")
    void generateAccessToken_NullNickname_NoNicknameClaim() {
        // given
        Authentication authentication = createAuthentication("test@example.com");

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(authentication, null);

        // then
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(claims.get("nickname")).isNull();
    }

    @Test
    @DisplayName("nickname이 빈 문자열인 경우 claim에 포함되지 않음")
    void generateAccessToken_BlankNickname_NoNicknameClaim() {
        // given
        Authentication authentication = createAuthentication("test@example.com");

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(authentication, "   ");

        // then
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(claims.get("nickname")).isNull();
    }

    @Test
    @DisplayName("기본 generateAccessToken은 nickname 없이 호출 가능")
    void generateAccessToken_WithoutNickname_Works() {
        // given
        Authentication authentication = createAuthentication("test@example.com");

        // when
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // then
        assertThat(accessToken).isNotBlank();

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("test@example.com");
        assertThat(claims.get("auth")).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Access Token 유효성 검증 성공")
    void validateToken_ValidToken_ReturnsTrue() {
        // given
        Authentication authentication = createAuthentication("test@example.com");
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // when
        boolean isValid = jwtTokenProvider.validateToken(accessToken);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 Token 유효성 검증 실패")
    void validateToken_InvalidToken_ReturnsFalse() {
        // when
        boolean isValid = jwtTokenProvider.validateToken("invalid-token");

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Refresh Token 생성")
    void generateRefreshToken_ReturnsUUID() {
        // when
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // then
        assertThat(refreshToken).isNotBlank();
        assertThat(refreshToken).matches("[a-f0-9\\-]{36}"); // UUID format
    }

    @Test
    @DisplayName("Authentication 객체 복원")
    void getAuthentication_ValidToken_ReturnsAuthentication() {
        // given
        Authentication authentication = createAuthentication("test@example.com");
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // when
        Authentication restoredAuth = jwtTokenProvider.getAuthentication(accessToken);

        // then
        assertThat(restoredAuth.getName()).isEqualTo("test@example.com");
        assertThat(restoredAuth.getAuthorities()).hasSize(1);
    }
}
