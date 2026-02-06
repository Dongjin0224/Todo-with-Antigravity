# 📝 Todo App

> Spring Boot + Vanilla JS로 만든 풀스택 TODO 애플리케이션

## 🛠️ 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL

### Frontend
- HTML5
- CSS3
- Vanilla JavaScript (fetch API)

## 📁 프로젝트 구조

```
Todo/
├── backend/                 # Spring Boot REST API
│   └── src/main/java/
│       └── com/todo/
│           ├── controller/  # REST 컨트롤러
│           ├── service/     # 비즈니스 로직
│           ├── repository/  # JPA 리포지토리
│           ├── entity/      # 엔티티
│           ├── dto/         # 요청/응답 DTO
│           └── exception/   # 예외 처리
│
├── index.html              # 메인 페이지
├── style.css               # 스타일시트
└── app.js                  # 프론트엔드 로직
```

## 🚀 실행 방법

### 1. PostgreSQL 설정

```bash
# Docker로 PostgreSQL 실행
docker run --name todo-db -e POSTGRES_PASSWORD=password -e POSTGRES_DB=tododb -p 5432:5432 -d postgres
```

### 2. Backend 실행

```bash
cd backend
./gradlew bootRun
```

### 3. Frontend 실행

```bash
# 정적 파일 서버로 실행 (예: Live Server)
# 또는 브라우저에서 index.html 직접 열기
```

## 📌 API Endpoints

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/todos` | 전체 할일 조회 |
| GET | `/api/todos/{id}` | 특정 할일 조회 |
| POST | `/api/todos` | 할일 생성 |
| PUT | `/api/todos/{id}` | 할일 수정 |
| DELETE | `/api/todos/{id}` | 할일 삭제 |

## 📚 학습 포인트

- Spring Boot 레이어드 아키텍처 (Controller → Service → Repository)
- DTO 패턴을 통한 API 데이터 전송
- 전역 예외 처리 (@RestControllerAdvice)
- 프론트/백엔드 분리 및 CORS 설정
- fetch API를 통한 REST API 통신

---

> 🗓️ Created: 2026-02 • With Antigravity
