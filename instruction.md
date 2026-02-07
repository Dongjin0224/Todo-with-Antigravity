# 프로젝트 실행 및 API 문서 확인 가이드

## 1. 프로젝트 실행

### Backend 실행
```bash
cd backend
./gradlew bootRun
```
- 서버는 기본적으로 `http://localhost:8080` 포트에서 실행됩니다.
- PostgreSQL과 Redis는 `docker-compose up -d`로 실행 중이어야 합니다.

## 2. API 문서 확인 (Swagger UI)

Swagger UI를 통해 API 명세를 확인하고 직접 테스트할 수 있습니다.

- **접속 주소**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **문서 생성**: API 문서는 테스트 기반으로 자동 생성됩니다.
  ```bash
  cd backend
  ./gradlew test copyOpenApiSpec
  ```

### 인증 (JWT Token)
대부분의 API는 인증이 필요합니다.

1. Swagger UI 우측 상단 **Authorize** 버튼 클릭
2. `bearerAuth` 필드에 Access Token 입력 (Bearer 접두사 없이 토큰만 입력)
3. **Authorize** 클릭 후 닫기
4. 이후 요청 시 자동으로 인증 헤더가 포함됩니다.

### 테스트 계정 생성 예시
```bash
# 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com", "password":"test1234!", "nickname":"TestUser"}'

# 로그인 (토큰 발급)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com", "password":"test1234!"}'
```

---

## 3. 개발 가이드 (Swagger + REST Docs)

이 프로젝트는 `Spring REST Docs`와 `Swagger UI`를 결합하여 사용합니다.

1. **테스트 코드 작성**: `MockMvc`와 `RestDocumentationRequestBuilders`를 사용하여 컨트롤러 테스트를 작성합니다.
2. **문서화**: 테스트 실행 시 `build/generated-snippets`에 스니펫이 생성되고, `openapi3` task가 이를 OpenAPI 스펙(`openapi3.json`)으로 변환합니다.
3. **UI 반영**: `copyOpenApiSpec` task가 생성된 스펙을 `static/docs/`로 복사하여 Swagger UI에서 로드합니다.

