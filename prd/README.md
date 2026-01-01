# 송금 서비스 PRD (Product Requirements Document)

## 개요

계좌 간 송금 시스템을 DDD + CQRS + 멀티모듈 아키텍처로 구현합니다.

---

## 문서 목차

| 문서 | 설명 |
|------|------|
| [01-common.md](./01-common.md) | 공통 사항 (Entity 구조, 도메인 예외, 코딩 컨벤션) |
| [02-register-account.md](./02-register-account.md) | API 1: 계좌 등록 |
| [03-delete-account.md](./03-delete-account.md) | API 2: 계좌 삭제 |
| [04-deposit.md](./04-deposit.md) | API 3: 입금 |
| [05-withdraw.md](./05-withdraw.md) | API 4: 출금 (일일 한도 100만 원) |
| [06-transfer.md](./06-transfer.md) | API 5: 이체 (일일 한도 300만 원, 수수료 1%) |
| [07-activities.md](./07-activities.md) | API 6: 거래내역 조회 (CQRS Query) |
| [10-get-account.md](./10-get-account.md) | API 7: 계좌 단건 조회 |
| [11-get-accounts.md](./11-get-accounts.md) | API 8: 계좌 목록 조회 (페이지네이션) |
| [08-evaluation-checklist.md](./08-evaluation-checklist.md) | 평가 항목 체크리스트 |
| [09-preferred-features-checklist.md](./09-preferred-features-checklist.md) | 우대사항 체크리스트 |

---

## 기술적 요구사항

- **Java 17+**
- **Spring Boot 3.2+** & JPA (Hibernate)
- **Docker & Docker Compose**
- **Flyway** (DB 마이그레이션)

---

## 아키텍처

```
leesuchan_backend/
├── account/          # Account Aggregate (계좌 도메인)
├── activity/         # Activity Aggregate (거래내역)
├── common/           # 공통 모듈 (ApiResponse, Error)
├── service/          # 웹 계층 (Controller)
└── infra/            # 인프라 계층
    ├── database/     # JPA 영속성
    ├── external/     # 외부 API 연동
    └── flyway/       # DB 마이그레이션
```

---

## 설계 원칙

- **DDD**: Aggregate Root는 JPA Entity로 직접 구현
- **CQRS**: Command는 account/service, Query는 activity/service
- **동시성**: JPA 낙관적 락 (`@Version`) + 재시료 메커니즘
- **확장성**: Port-Interface 패턴으로 인프라 변경 시 도메인 로직 영향 최소화

---

## 한도 규칙 요약

| 구분 | 출금 | 이체 |
|------|------|------|
| **일일 한도** | 1,000,000원 | 3,000,000원 |
| **수수료** | 없음 | 이체 금액의 1% |
| **한도 리셋** | 날짜 변경 시 | 날짜 변경 시 |
| **한도 누적 필드** | `dailyWithdrawAmount` | `dailyTransferAmount` |
| **관련 예외** | `DailyWithdrawLimitExceededException` | `DailyTransferLimitExceededException` |

---

## 실행 방법

```bash
# 1. Docker Compose로 DB 실행
docker-compose up -d

# 2. 애플리케이션 실행
./gradlew :service:bootRun

# 3. 테스트
./gradlew test
```
