# 기여 가이드 (Contributing Guide)

프로젝트에 기여해주셔서 감사합니다! 🎉
아래 규칙을 따라주시면 원활한 협업이 가능합니다.

## 🔀 브랜치 전략 (Git Flow)

- `main`: 배포 가능한 안정적인 상태
- `develop`: 개발 중인 최신 상태 (선택 사항)
- `feature/*`: 새로운 기능 개발
  - 예: `feature/dark-mode`, `feature/login`
- `fix/*`: 버그 수정
  - 예: `fix/login-error`

## 📝 커밋 메시지 규칙

| 타입 | 설명 | 예시 |
|------|------|------|
| `feat` | 새로운 기능 추가 | `feat: 다크모드 기능 추가` |
| `fix` | 버그 수정 | `fix: 로그인 오류 수정` |
| `docs` | 문서 수정 | `docs: README 업데이트` |
| `style` | 코드 포맷팅 (로직 변경 X) | `style: 세미콜론 추가` |
| `refactor` | 코드 리팩터링 | `refactor: 함수 분리` |

## 🚀 PR 프로세스

1. Issue 생성 (할 일 정의)
2. Branch 생성 (`feature/기능이름`)
3. 개발 및 커밋
4. Pull Request 생성 (Template 작성 필수)
5. Review 및 Merge
