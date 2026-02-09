package com.todo.config;

import com.todo.service.CustomOAuth2UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * 테스트 환경에서 OAuth2 관련 빈들을 모킹하기 위한 설정 클래스
 */
@TestConfiguration
public class TestSecurityConfig {

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private AppProperties appProperties;
}
