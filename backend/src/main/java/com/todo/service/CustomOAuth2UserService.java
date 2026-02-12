package com.todo.service;

import com.todo.entity.Member;
import com.todo.entity.Member.Provider;
import com.todo.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        String providerId;
        String email;
        String nickname;
        boolean isEmailVerified = false;

        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (provider == Provider.GOOGLE) {
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
            // Google의 이메일 검증 여부 확인
            Object emailVerified = attributes.get("email_verified");
            isEmailVerified = Boolean.TRUE.equals(emailVerified);
        } else if (provider == Provider.KAKAO) {
            providerId = String.valueOf(attributes.get("id"));
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount == null) {
                throw new OAuth2AuthenticationException("Kakao account 정보가 없습니다.");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            nickname = profile != null && profile.get("nickname") != null
                    ? String.valueOf(profile.get("nickname"))
                    : "kakao_user";

            // Kakao의 이메일 검증 여부 확인
            Object emailVerifiedObj = kakaoAccount.get("is_email_verified");
            isEmailVerified = Boolean.TRUE.equals(emailVerifiedObj);
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("OAuth provider가 이메일 정보를 제공하지 않았습니다.");
        }

        // 이메일 검증 여부 로깅
        if (!isEmailVerified) {
            log.warn("OAuth 이메일이 미검증 상태입니다: provider={}, email={}", provider, maskEmail(email));
        }

        log.info("OAuth2 로그인 시도: provider={}, email={}", provider, maskEmail(email));

        // 기존 회원 조회 (이메일 기준)
        Optional<Member> existingMember = memberRepository.findByEmail(email);

        Member member;
        if (existingMember.isPresent()) {
            member = existingMember.get();

            // 기존 계정이 LOCAL인 경우 OAuth 연동
            if (member.getProvider() == Provider.LOCAL) {
                if (!isEmailVerified) {
                    throw new OAuth2AuthenticationException("이메일 검증이 완료된 계정만 OAuth 연동할 수 있습니다.");
                }
                member.linkOAuthAccount(provider, providerId);
                log.info("기존 LOCAL 계정과 OAuth 연동: email={}, provider={}", maskEmail(email), provider);
            }
            // 같은 OAuth provider로 로그인하는 경우 - 정상 로그인
            else if (member.getProvider() == provider) {
                log.debug("기존 OAuth 계정으로 로그인: email={}, provider={}", maskEmail(email), provider);
            }
            // 다른 OAuth provider로 로그인 시도하는 경우 - 기존 계정 사용
            else {
                log.info("다른 OAuth provider로 로그인 시도 - 기존 계정 사용: email={}, 기존provider={}, 시도provider={}",
                        maskEmail(email), member.getProvider(), provider);
            }
        } else {
            // 신규 회원 생성
            member = Member.builder()
                    .email(email)
                    .nickname(nickname)
                    .role(Member.Role.USER)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            memberRepository.save(member);
            log.info("OAuth 신규 회원 생성: email={}", maskEmail(email));
        }

        return new DefaultOAuth2User(
                Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + member.getRole().name())),
                attributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                        .getUserNameAttributeName());
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
