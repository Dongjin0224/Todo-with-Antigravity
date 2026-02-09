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

        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (provider == Provider.GOOGLE) {
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
        } else if (provider == Provider.KAKAO) {
            providerId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        log.info("OAuth2 로그인 시도: provider={}, email={}, nickname={}", provider, email, nickname);

        // 기존 회원 조회 (이메일 기준)
        Optional<Member> existingMember = memberRepository.findByEmail(email);

        Member member;
        if (existingMember.isPresent()) {
            member = existingMember.get();
            // 기존 계정에 OAuth 연동
            if (member.getProvider() == Provider.LOCAL) {
                member.linkOAuthAccount(provider, providerId);
                log.info("기존 계정과 OAuth 연동: email={}", email);
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
            log.info("OAuth 신규 회원 생성: email={}", email);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + member.getRole().name())),
                attributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                        .getUserNameAttributeName());
    }
}
