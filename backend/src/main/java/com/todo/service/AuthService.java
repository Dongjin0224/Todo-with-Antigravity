package com.todo.service;

import com.todo.config.JwtTokenProvider;
import com.todo.dto.AuthRequest;
import com.todo.dto.AuthResponse;
import com.todo.entity.Member;
import com.todo.repository.MemberRepository;
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

    @Transactional
    public AuthResponse signup(AuthRequest authRequest) {
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

        // 회원가입 후 자동 로그인 처리 없음 -> 로그인 유도
        return null;
    }

    @Transactional
    public AuthResponse login(AuthRequest authRequest) {
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(), authRequest.getPassword());

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        // authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername
        // 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(authentication);

        // 4. 토큰 발급 (Nickname 등 추가 정보 포함 가능하게)
        Member member = memberRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));

        return AuthResponse.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpiresIn(1000 * 60 * 30L) // 30분
                .nickname(member.getNickname())
                .email(member.getEmail())
                .role(member.getRole().name())
                .build();
    }
}
