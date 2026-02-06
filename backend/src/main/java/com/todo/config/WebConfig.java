package com.todo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 설정 (CORS 전역 설정)
 * 
 * CORS (Cross-Origin Resource Sharing):
 * - 브라우저 보안 정책으로, 다른 도메인의 API 호출을 기본적으로 차단
 * - 프론트엔드(file:// 또는 localhost:3000)에서 
 *   백엔드(localhost:8080) 호출하려면 CORS 설정 필요
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")      // /api로 시작하는 모든 경로
                .allowedOrigins("*")         // 모든 도메인 허용 (개발용)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);               // preflight 캐시 1시간
    }
}
