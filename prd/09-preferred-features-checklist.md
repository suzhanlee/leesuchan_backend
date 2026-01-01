# 우대사항 체크리스트

평가 우대사항별 구현 현황을 체크합니다.

---

## 1. 동시성 이슈 고려

### 1.1 낙관적 락 (Optimistic Locking)

| 항목 | 상태 | 설명 |
|------|------|------|
| @Version 필드 | ✅ | Account Entity에 JPA @Version 사용 |
| 버전 충돌 감지 | ✅ | JPA가 자동으로 OptimisticLockingFailureException 발생 |
| 동시 업데이트 방지 | ✅ | DB version 필드로 충돌 감지 |

**구현 예시**:
```java
@Entity
public class Account {
    @Version
    private Long version;
}
```

### 1.2 재시도 메커니즘 (Retry Mechanism)

| 항목 | 상태 | 설명 |
|------|------|------|
| @Retryable | ❌ 미구현 | 낙관적 락 충돌 시 재시도 없음 |
| Spring Retry | ❌ 미설정 | 의존성 추가 필요 |

**구현 제안**:
```java
@Retryable(
    retryFor = OptimisticLockingFailureException.class,
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
@Transactional
public void transfer(String fromAccountNumber, String toAccountNumber, long amount) {
    // ...
}
```

### 1.3 비관적 락 (Pessimistic Locking)

| 항목 | 상태 | 설명 |
|------|------|------|
| @Lock(LockModeType.PESSIMISTIC_WRITE) | ❌ 미구현 | 필요시 고려 |
| SELECT FOR UPDATE | ❌ 미사용 | JPA 쿼리 메서드에 설정 가능 |

---

## 2. API 명세 작성

### 2.1 OpenAPI/Swagger

| 항목 | 상태 | 설명 |
|------|------|------|
| springdoc-openapi 의존성 | ❌ 미추가 | build.gradle에 추가 필요 |
| Swagger UI | ❌ 미설정 | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | ❌ 미설정 | http://localhost:8080/v3/api-docs |
| @Schema 어노테이션 | ❌ 미사용 | DTO 필드 설명 추가 |
| @Operation 어노테이션 | ❌ 미사용 | API 엔드포인트 설명 추가 |

**구현 제안 - build.gradle**:
```groovy
dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
}
```

**구현 제안 - 설정 클래스**:
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("송금 서비스 API")
                        .version("1.0")
                        .description("계좌 간 송금 시스템"));
    }
}
```

---

## 3. 추가 요구사항 외 기능

### 3.1 계좌 단건 조회

| 항목 | 상태 | 설명 |
|------|------|------|
| GET /api/accounts/{accountNumber} | ⚠️ TODO | AccountController에 TODO로 남음 |

**구현 필요 사항**:
- GetAccountQueryService 또는 AccountRepository 직접 조회
- AccountResponse로 변환
- AccountNotFoundException 처리

### 3.2 계좌 목록 조회

| 항목 | 상태 | 설명 |
|------|------|------|
| GET /api/accounts | ❌ 미구현 | 전체 계좌 목록 또는 페이지네이션 필요 |

**구현 제안**:
```java
@GetMapping
public ApiResponse<Page<AccountResponse>> getAccounts(
    @PageableDefault(size = 20) Pageable pageable
) {
    Page<Account> accounts = getAccountsQueryService.getAccounts(pageable);
    return ApiResponse.success(accounts.map(AccountResponse::from));
}
```

### 3.3 페이지네이션 (Pagination)

| 항목 | 상태 | 설명 |
|------|------|------|
| 거래내역 페이지네이션 | ❌ 미구현 | 전체 조회만 지원 |
| Spring Data Pageable | ❌ 미사용 | Page<T> 반환 필요 |

**구현 제안**:
```java
// GetActivitiesQueryService
public Page<Activity> getActivities(String accountNumber, Pageable pageable) {
    return activityRepository.findByAccountAccountNumber(accountNumber, pageable);
}
```

### 3.4 Health Check

| 항목 | 상태 | 설명 |
|------|------|------|
| /actuator/health | ❌ 미설정 | Spring Boot Actuator 의존성 필요 |

**구현 제안 - build.gradle**:
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

**구현 제안 - application.yml**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always
```

### 3.5 메트릭/모니터링

| 항목 | 상태 | 설명 |
|------|------|------|
| Micrometer | ❌ 미설정 | 메트릭 수집 라이브러리 |
| Prometheus | ❌ 미설정 | 메트릭 내보내기 |
| Custom Metrics | ❌ 미구현 | 비즈니스 메트릭 (송금 금액, 건수 등) |

---

## 4. 멀티모듈 아키텍처

### 4.1 모듈 구조

| 모듈 | 상태 | 설명 |
|------|------|------|
| account | ✅ | Account Aggregate (계좌 도메인) |
| activity | ✅ | Activity Aggregate (거래내역) |
| common | ✅ | 공통 모듈 (ApiResponse, Error) |
| service | ✅ | 웹 계층 (Controller) |
| infra/database | ✅ | JPA 영속성 |
| infra/external | ✅ | 외부 API 연동 (구조 준비됨) |
| infra/flyway | ✅ | DB 마이그레이션 |

### 4.2 의존성 방향

```
service → account, activity, common
account → common
activity → common
infra/database → account, activity, common
```

| 항목 | 상태 |
|------|------|
| 도메인 계층 독립성 | ✅ |
| 인프라 의존성 역전 | ✅ |
| 모듈 간 순환 의존 | ✅ 없음 |

---

## 5. 확장성 (인프라 변경 가능성)

### 5.1 Repository 추상화

| 항목 | 상태 | 설명 |
|------|------|------|
| Port 인터페이스 | ✅ | 도메인 계층에 AccountRepository 인터페이스 |
| Adapter 구현체 | ✅ | infra/database에 AccountRepositoryImpl |
| JPA 교체 가능성 | ✅ | NoSQL 등으로 교체 시 도메인 로직 무영향 |

### 5.2 영속성 독립성

| 항목 | 상태 | 설명 |
|------|------|------|
| 도메인 모델 순수성 | ✅ | JPA 어노테이션만 제거하면 POJO |
| @Entity 포트 분리 | ❌ | 현재는 도메인 모델에 직접 @Entity |

**개선 제안 (선택사항)**:
- 도메인 모델과 JPA Entity 분리 (별도 패키지)
- EntityMapper로 변환 (추가 복잡도 vs 순수 도메인)

### 5.3 외부 API 연동

| 항목 | 상태 | 설명 |
|------|------|------|
| infra/external 모듈 | ✅ 구조 준비 | 실제 연동 없음 |
| Port 인터페이스 | ⚠️ | 필요시 정의 |
| 테스트 더블 | ✅ | @MockBean으로 Mock 가능 |

### 5.4 설정 외부화

| 항목 | 현재 상태 | 개선 필요 |
|------|----------|----------|
| 출금 한도 (100만 원) | 코드 상수 | ⚠️ @Value or @ConfigurationProperties |
| 이체 한도 (300만 원) | 코드 상수 | ⚠️ @Value or @ConfigurationProperties |
| 이체 수수료 (1%) | 코드 상수 | ⚠️ @Value or @ConfigurationProperties |

**구현 제안**:
```yaml
# application.yml
account:
  limit:
    daily-withdraw: 1000000
    daily-transfer: 3000000
  fee:
    transfer-rate: 0.01
```

```java
@Component
public class AccountLimitConfig {
    @Value("${account.limit.daily-withdraw}")
    private long dailyWithdrawLimit;

    @Value("${account.limit.daily-transfer}")
    private long dailyTransferLimit;

    @Value("${account.fee.transfer-rate}")
    private double transferFeeRate;
}
```

---

## 6. 추가 개선사항 (Optional)

| 항목 | 상태 | 설명 |
|------|------|------|
| API 버전 관리 (/api/v1/) | ❌ | @RequestMapping("/api/v1/accounts") |
| Rate Limiting | ❌ | API 요청 속도 제한 |
| 인증/인가 | ❌ | Spring Security, JWT |
| 로깅 | ⚠️ | 기본 로그만 존재 |
| 트레이싱 | ❌ | MDC, Sleuth |
| 캐싱 | ❌ | Redis, Caffeine |
| 비동기 처리 | ❌ | @Async, Message Queue |
| 분산 트랜잭션 | ❌ | Saga, Outbox Pattern |

---

## 7. 관련 파일

| 파일 | 경로 |
|------|------|
| Account Entity | `account/src/main/java/com/leesuchan/account/domain/model/Account.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/controller/AccountController.java` |
| build.gradle | `build.gradle` |
| application.yml | `service/src/main/resources/application.yml` |
