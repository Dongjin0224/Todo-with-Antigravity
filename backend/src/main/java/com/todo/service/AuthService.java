package com.todo.service;

import com.todo.config.JwtTokenProvider;
import com.todo.dto.AuthRequest;
import com.todo.dto.AuthResponse;
import com.todo.entity.Member;
import com.todo.entity.RefreshToken;
import com.todo.repository.MemberRepository;
import com.todo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final MemberRepository memberRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final RefreshTokenRepository refreshTokenRepository;

        @Transactional
        public void signup(AuthRequest authRequest) {
                if (memberRepository.existsByEmail(authRequest.getEmail())) {
                        throw new RuntimeException("이미 가입되어 있는 유저입니다");
                }

                Member member = Member.builder()
                                .email(authRequest.getEmail())
                                .password(passwordEncoder.encode(authRequest.getPassword()))
                                .nickname(authRequest.getNickname())
                                .role(Member.Role.USER)
                                .build();

                memberRepository.save(member);
        }

        @Transactional
        public AuthResponse login(AuthRequest authRequest) {
                // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                authRequest.getEmail(), authRequest.getPassword());

                // 2. 실제로 검증 (사용자 비밀번호 체크)
                Authentication authentication = authenticationManagerBuilder.getObject()
                                .authenticate(authenticationToken);

                // 3. 인증 정보를 기반으로 JWT 토큰 생성
                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                String refreshToken = jwtTokenProvider.generateRefreshToken();

                // 4. RefreshToken Redis 저장
                Member member = memberRepository.findByEmail(authRequest.getEmail())
                                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));

                RefreshToken refreshTokenEntity = RefreshToken.builder()
                                .id(member.getId().toString())
                                .refreshToken(refreshToken)
                                .expiration(jwtTokenProvider.getRefreshTokenValidity())
                                .build();

                refreshTokenRepository.save(refreshTokenEntity);

                return AuthResponse.builder()
                                .grantType("Bearer")
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .nickname(member.getNickname())
                                .email(member.getEmail())
                                .role(member.getRole().name())
                                .build();
        }

        @Transactional
        public AuthResponse reissue(String refreshToken) {
                // 1. Refresh Token 검증
                RefreshToken savedToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token입니다."));

                // 2. Member 조회
                Member member = memberRepository.findById(Long.parseLong(savedToken.getId()))
                                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));

                // 3. 새로운 토큰 발급
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                member.getEmail(), null,
                                java.util.List.of(
                                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                "ROLE_" + member.getRole().name())));
                String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken();

                // 4. Refresh Token Rotation (기존 삭제 후 새로 저장)
                savedToken.updateToken(newRefreshToken, jwtTokenProvider.getRefreshTokenValidity());
                refreshTokenRepository.save(savedToken);

                return AuthResponse.builder()
                                .grantType("Bearer")
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken)
                                .nickname(member.getNickname())
                                .email(member.getEmail())
                                .role(member.getRole().name())
                                .build();
        }

        @Transactional
        public void logout(String email) {
                Member member = memberRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
                refreshTokenRepository.deleteById(member.getId().toString());
        }
}
