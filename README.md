# leesuchan_backend

계좌 간 송금 시스템을 DDD + CQRS + 멀티모듈 아키텍처로 구현한 프로젝트입니다.

## 개요

본 프로젝트는 다음과 같은 핵심 기능을 제공하는 송금 서비스입니다:
- 계좌 등록/조회/삭제
- 입금, 출금, 계좌 간 이체
- 거래 내역 조회
- 일일 한도 관리
- 수수료 계산

## 아키텍처

### 멀티모듈 구조
```
leesuchan_backend/
├── account/          # Account Aggregate (계좌 도메인)
├── activity/         # Activity Aggregate (거래내역 도메인)
├── common/           # 공통 모듈 (ApiResponse, Error)
├── service/          # 웹 계층 (Controller, Query Service)
├── infra/            # 인프라 계층
│   ├── database/     # JPA 영속성
│   └── flyway/       # DB 마이그레이션
└── build.gradle.kts  # 루트 빌드 스크립트
```

### DDD (Domain-Driven Design)
- **Aggregate**: Account, Activity
- **Repository Pattern**: Port 인터페이스와 구현체 분리
- **Domain Exception**: 도메인별 예외 체계 구현
- **Value Object**: DailyLimitTracker 등 불변 VO 사용

### CQRS (Command Query Responsibility Segregation)
- **Command**: UseCase (등록, 입금, 출금, 이체)
- **Query**: Query Service (조회 전용)

### 기타 패턴
- **Port & Adapter**: 외부 의존성 분리
- **낙관적 락**: JPA `@Version`으로 동시성 제어
- **설정 외부화**: 한도 규칙, 수수료율 외부에서 관리

## 기술 스택

- **Java 17**
- **Spring Boot 3.2**
- **Spring Data JPA** (Hibernate)
- **MySQL 8.0**
- **Flyway** (DB 마이그레이션)
- **SpringDoc OpenAPI 3.0** (Swagger UI)
- **Spring Retry** (재시도 메커니즘)
- **Gradle 8.13**

## 주요 기능

### 1. 계좌 관리
- 계좌 등록: `POST /api/v1/accounts`
- 계좌 조회: `GET /api/v1/accounts/{accountNumber}`
- 계좌 목록: `GET /api/v1/accounts`
- 계좌 삭제: `DELETE /api/v1/accounts/{accountNumber}` (소프트 삭제)

### 2. 입금
- `POST /api/v1/transactions/deposit`
- 특정 계좌에 금액 입금

### 3. 출금
- `POST /api/v1/transactions/withdraw`
- 특정 계좌에서 금액 출금
- **일일 한도**: 1,000,000원

### 4. 이체
- `POST /api/v1/transactions/transfer`
- 계좌 간 자금 이체
- **일일 한도**: 3,000,000원
- **수수료**: 이체 금액의 1%

### 5. 거래내역 조회
- `GET /api/v1/activities/{accountNumber}`
- 최신순 정렬

## 동시성 처리

- **낙관적 락**: JPA `@Version`으로 구현
- **재시도 메커니즘**: Spring Retry로 자동 재시도 (최대 3회)
- **예외 처리**: `OptimisticLockingFailureException` 발생 시 사용자 친화적 메시지 반환

## 실행 방법

### 방법 1: Docker Compose로 전체 서비스 실행 (권장)

```bash
# MySQL + 애플리케이션 한 번에 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 중지
docker-compose down
```

### 방법 2: 로컬에서 실행 (개발용)

```bash
# 1. Docker Compose로 MySQL만 실행
docker-compose up -d mysql

# 2. 애플리케이션 실행
./gradlew :service:bootRun
```

### API 접속

Swagger UI: http://localhost:8080/swagger-ui.html

## 테스트

- **단위 테스트**: 13개 파일 (UseCase, Domain 테스트)
- **E2E 테스트**: 9개 파일 (API 호출 테스트)

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :account:test
./gradlew :service:test
```

## API 명세

### 계좌 관리
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/accounts` | 계좌 등록 |
| GET | `/api/v1/accounts/{accountNumber}` | 계좌 조회 |
| GET | `/api/v1/accounts` | 계좌 목록 (페이징) |
| DELETE | `/api/v1/accounts/{accountNumber}` | 계좌 삭제 |

### 거래
| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/transactions/deposit` | 입금 |
| POST | `/api/v1/transactions/withdraw` | 출금 |
| POST | `/api/v1/transactions/transfer` | 이체 |

### 거래내역
| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/v1/activities/{accountNumber}` | 거래내역 조회 (최신순) |

## 한도 규칙

| 항목 | 한도 |
|------|------|
| 일일 출금 한도 | 1,000,000원 |
| 일일 이체 한도 | 3,000,000원 |
| 이체 수수료율 | 1% |

## 데이터베이스 스키마

### account 테이블
- `id` (PK)
- `account_number` (UQ)
- `account_name`
- `balance`
- `daily_withdraw_limit`
- `daily_transfer_limit`
- `created_at`
- `updated_at`
- `deleted_at` (소프트 삭제)
- `version` (낙관적 락)

### activity 테이블
- `id` (PK)
- `account_id` (FK)
- `activity_type` (DEPOSIT, WITHDRAW, TRANSFER_OUT, TRANSFER_IN)
- `amount`
- `fee`
- `balance_after`
- `transaction_id`
- `target_account_id`
- `target_account_number`
- `created_at`

## 커밋 규칙

- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **refactor**: 리팩토링
- **test**: 테스트 코드
- **docs**: 문서 수정
- **config**: 설정 파일
