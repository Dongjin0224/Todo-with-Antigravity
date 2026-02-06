# 📝 Todo App

> Spring Boot + Next.js로 만든 풀스택 TODO 애플리케이션

## 🛠️ 기술 스택

### Backend
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL

### Frontend
- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: TailwindCSS v4
- **State Management**: React Hooks
- **Drag & Drop**: dnd-kit

## 📁 프로젝트 구조

```
Todo/
├── backend/                 # Spring Boot REST API
├── frontend-next/           # Next.js Frontend (New)
│   ├── app/                 # App Router
│   ├── components/          # React Components
│   └── lib/                 # Utilities & API
└── frontend-vanilla/        # Legacy Vanilla JS (Archived)
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

### 3. Frontend 실행 (Next.js)

```bash
cd frontend-next
npm install
npm run dev
# 접속: http://localhost:3000
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
