# 송금 서비스 아키텍처

## 개요
계좌 간 송금 시스템을 DDD + CQRS + 멀티모듈 아키텍처로 구현합니다.

## 아키텍처

### 멀티모듈 구조
```
leesuchan_backend/
├── account/          # Account Aggregate
├── activity/         # Activity Aggregate (거래내역)
├── common/           # 공통 모듈 (ApiResponse, Error, AggregateRoot)
├── service/          # 웹 계층 (Controller)
└── infra/            # 인프라 계층
    ├── database/     # JPA 영속성
    ├── external/     # 외부 API 연동
    └── flyway/       # DB 마이그레이션
```

### DDD 설계
- **Account Aggregate**: 계좌, 잔액, 일일 한도 관리
- **Activity Aggregate**: 거래 내역 (입금/출금/이체)
- **Aggregate Root**: `AbstractAggregateRoot` 상속, 소프트 삭제 지원

### CQRS
- **Command**: account/service (등록, 삭제, 입금, 출금, 이체)
- **Query**: activity/service (거래 내역 조회)

### 동시성
- JPA 낙관적 락 (`@Version`)
- 재시도 메커니즘

## 한도 규칙
- 출금 한도: 일일 1,000,000원
- 이체 한도: 일일 3,000,000원
- 이체 수수료: 이체 금액의 1%

## 실행 방법
```bash
docker-compose up -d
./gradlew :service:bootRun
```

## 커밋 규칙 (Commit Convention)

### 커밋 메시지 형식
```
<타입>: <제목>

<본문 (선택)>
```

### 타입 (Type)
| 타입 | 설명 | 예시 |
|------|------|------|
| feat | 새로운 기능 추가 | feat: 계좌 등록 API 구현 |
| fix | 버그 수정 | fix: 출금 한도 체크 로직 수정 |
| docs | 문서 수정 | docs: README 실행 방법 추가 |
| style | 코드 스타일 (포맷팅) | style: import 정리 |
| refactor | 리팩토링 | refactor: Account 잔액 로직 분리 |
| test | 테스트 코드 | test: AccountService 단위 테스트 추가 |
| chore | 빌드/설정 | chore: Lombok 의존성 추가 |
| config | 설정 파일 | config: JPA Auditing 설정 추가 |

### 예시
```
feat: Account Aggregate 도메인 구현

- Account 엔티티 추가 (AbstractAggregateRoot 상속)
- Money Value Object 추가
- AccountRepository Port 인터페이스 추가
- 소프트 삭제 지원 (deletedAt)
```

### 주의사항
- 제목은 한국어로 작성
- 제목은 50자 이내
- 제목 끝에 마침표(.) 사용하지 않음
- 본문은 어떻게보다 무엇을/왜 변경했는지 작성
- 한 기능 완성 시마다 커밋 (한꺼번에 몰아서 커밋 금지)
