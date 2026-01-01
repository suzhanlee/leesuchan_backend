# 평가 항목 체크리스트

송금 서비스 구현에 대한 평가 항목별 구현 현황을 체크합니다.

---

## 1. API 명세 구현 여부

### 1.1 필수 API 구현 현황

| API | 상태 | 비고 |
|-----|------|------|
| 계좌 등록 | ✅ 구현됨 | POST /api/accounts |
| 계좌 삭제 | ✅ 구현됨 | DELETE /api/accounts/{accountNumber} |
| 입금 | ✅ 구현됨 | POST /api/accounts/deposit |
| 출금 | ✅ 구현됨 | POST /api/accounts/withdraw (일일 한도 100만 원) |
| 이체 (송금) | ✅ 구현됨 | POST /api/accounts/transfer (일일 한도 300만 원, 수수료 1%) |
| 거래내역 조회 | ✅ 구현됨 | GET /api/accounts/{accountNumber}/activities |
| **계좌 단건 조회** | ⚠️ TODO | GET /api/accounts/{accountNumber} |
| **계좌 목록 조회** | ❌ 미구현 | GET /api/accounts |

### 1.2 미구현 API 상세

#### GET /api/accounts/{accountNumber} - 계좌 단건 조회

**현재 상태**: AccountController에 TODO로 남아있음

**구현 필요 사항**:
- AccountRepository에서 계좌번호로 계좌 조회
- AccountResponse로 변환하여 반환
- 계좌 미존재 시 `AccountNotFoundException` 발생

---

## 2. 테이블 설계

### 2.1 Account 테이블

| 컬럼 | 타입 | 제약조건 | 상태 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | ✅ |
| account_number | VARCHAR(20) | UNIQUE, NOT NULL | ✅ |
| account_name | VARCHAR(100) | NOT NULL | ✅ |
| balance | BIGINT | NOT NULL, DEFAULT 0 | ✅ |
| daily_withdraw_amount | BIGINT | NOT NULL, DEFAULT 0 | ✅ |
| daily_transfer_amount | BIGINT | NOT NULL, DEFAULT 0 | ✅ |
| last_withdraw_date | DATE | NULL | ✅ |
| last_transfer_date | DATE | NULL | ✅ |
| created_at | DATETIME | NOT NULL | ✅ |
| updated_at | DATETIME | NOT NULL | ✅ |
| deleted_at | DATETIME | NULL | ✅ (소프트 삭제) |
| version | BIGINT | NOT NULL, DEFAULT 0 | ✅ (낙관적 락) |

### 2.2 Activity 테이블

| 컬럼 | 타입 | 제약조건 | 상태 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | ✅ |
| account_id | BIGINT | FK, NOT NULL | ✅ |
| activity_type | ENUM | NOT NULL | ✅ |
| amount | BIGINT | NOT NULL | ✅ |
| fee | BIGINT | NOT NULL, DEFAULT 0 | ✅ |
| balance_after | BIGINT | NOT NULL | ✅ |
| reference_account_id | BIGINT | NULL | ✅ |
| reference_account_number | VARCHAR(20) | NULL | ✅ |
| description | VARCHAR(200) | NULL | ✅ |
| transaction_id | VARCHAR(50) | NULL | ✅ |
| created_at | DATETIME | NOT NULL | ✅ |

### 2.3 인덱스 설계

| 인덱스 | 테이블 | 컬럼 | 상태 |
|--------|--------|------|------|
| idx_deleted_at | account | deleted_at | ✅ (소프트 deleted_at 조회 최적화) |
| idx_account_id | activity | account_id | ✅ (거래내역 조회 최적화) |
| idx_transaction_id | activity | transaction_id | ✅ (이체 입출금 쌍 조회) |

---

## 3. 확장성

### 3.1 멀티모듈 아키텍처

| 모듈 | 상태 | 설명 |
|------|------|------|
| account | ✅ | Account Aggregate (계좌 도메인) |
| activity | ✅ | Activity Aggregate (거래내역) |
| common | ✅ | 공통 모듈 (ApiResponse, Error) |
| service | ✅ | 웹 계층 (Controller) |
| infra/database | ✅ | JPA 영속성 |
| infra/external | ✅ | 외부 API 연동 (구조 준비됨) |
| infra/flyway | ✅ | DB 마이그레이션 |

### 3.2 DDD 설계

| 항목 | 상태 | 설명 |
|------|------|------|
| Aggregate Root | ✅ | Account, Activity Entity |
| 도메인 로직 캡슐화 | ✅ | Entity 내부에 비즈니스 로직 |
| Value Object | ⚠️ | WithdrawLimitTracker, TransferLimitTracker |
| 도메인 예외 | ✅ | 7개 도메인 예외 클래스 |
| 도메인 서비스 | ✅ | UseCase 클래스들 |

### 3.3 CQRS 패턴

| 구분 | 상태 | 설명 |
|------|------|------|
| Command | ✅ | account/service (등록, 삭제, 입금, 출금, 이체) |
| Query | ✅ | activity/service (거래내역 조회) |

### 3.4 Port-Adapter 패턴

| 항목 | 상태 | 설명 |
|------|------|------|
| Repository 인터페이스 | ✅ | 도메인 계층에 Port 정의 |
| Repository 구현체 | ✅ | infra/database에 구현체 |
| 의존성 방향 | ✅ | 도메인 → 인프라 (역전 의존) |

### 3.5 설정 외부화

| 항목 | 현재 상태 | 개선 필요 |
|------|----------|----------|
| 출금 한도 (100만 원) | 코드에 하드코딩 | ⚠️ application.yml로 이동 권장 |
| 이체 한도 (300만 원) | 코드에 하드코딩 | ⚠️ application.yml로 이동 권장 |
| 이체 수수료 (1%) | 코드에 하드코딩 | ⚠️ application.yml로 이동 권장 |

---

## 4. 테스트 코드 작성 여부

### 4.1 단위 테스트 (Unit Tests)

| 테스트 파일 | 상태 | 커버리지 |
|-------------|------|----------|
| AccountTest | ✅ | 도메인 로직 (생성, 입금, 출금, 이체) |
| ActivityTest | ✅ | 도메인 로직 |
| RegisterAccountUseCaseTest | ✅ | 계좌 등록 UseCase |
| DeleteAccountUseCaseTest | ✅ | 계좌 삭제 UseCase |
| DepositMoneyUseCaseTest | ✅ | 입금 UseCase |
| WithdrawMoneyUseCaseTest | ✅ | 출금 UseCase |
| TransferMoneyUseCaseTest | ✅ | 이체 UseCase |
| GetActivitiesQueryServiceTest | ✅ | 거래내역 조회 Query Service |

### 4.2 E2E 테스트 (Integration Tests)

| 테스트 파일 | 상태 | 커버리지 |
|-------------|------|----------|
| RegisterAccountE2ETest | ✅ | 계좌 등록 API |
| DeleteAccountE2ETest | ✅ | 계좌 삭제 API |
| DepositMoneyE2ETest | ✅ | 입금 API |
| WithdrawMoneyE2ETest | ✅ | 출금 API |
| TransferMoneyE2ETest | ✅ | 이체 API |
| GetActivitiesE2ETest | ✅ | 거래내역 조회 API |
| BeanValidationE2ETest | ✅ | Bean Validation 검증 |

### 4.3 테스트 프레임워크

| 항목 | 상태 |
|------|------|
| JUnit 5 | ✅ |
| Mockito | ✅ |
| AssertJ | ✅ |
| @SpringBootTest | ✅ |
| MockMvc | ✅ |

---

## 5. 예외 처리

### 5.1 도메인 예외 정의

| 예외 클래스 | 에러 코드 | 상태 |
|------------|----------|------|
| AccountNotFoundException | ACCOUNT_001 | ✅ |
| DuplicateAccountException | ACCOUNT_002 | ✅ |
| InvalidAccountNameException | ACCOUNT_003 | ✅ |
| InsufficientBalanceException | ACCOUNT_004 | ✅ |
| DailyWithdrawLimitExceededException | ACCOUNT_005 | ✅ |
| DailyTransferLimitExceededException | ACCOUNT_006 | ✅ |
| SameAccountTransferException | ACCOUNT_007 | ✅ |

### 5.2 예외 처리 구조

| 항목 | 상태 |
|------|------|
| DomainException 기반 예외 계층 | ✅ |
| ErrorCode 인터페이스 | ✅ |
| @ControllerAdvice 전역 예외 처리 | ✅ |
| 일관난 응답 형식 (ApiResponse) | ✅ |

---

## 6. 관련 파일

| 파일 | 경로 |
|------|------|
| AccountController | `service/src/main/java/com/leesuchan/service/controller/AccountController.java` |
| Account Entity | `account/src/main/java/com/leesuchan/account/domain/model/Account.java` |
| Activity Entity | `activity/src/main/java/com/leesuchan/activity/domain/model/Activity.java` |
| AccountErrorCode | `account/src/main/java/com/leesuchan/account/domain/exception/AccountErrorCode.java` |
