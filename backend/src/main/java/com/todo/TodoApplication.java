package com.todo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 메인 애플리케이션 클래스
 * 
 * @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 * - @Configuration: 이 클래스가 Bean 설정 클래스임을 표시
 * - @EnableAutoConfiguration: Spring Boot 자동 설정 활성화
 * - @ComponentScan: 현재 패키지 하위의 모든 컴포넌트 자동 스캔
 */
@SpringBootApplication
public class TodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}
