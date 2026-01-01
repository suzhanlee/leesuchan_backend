# API 6: 거래내역 조회

지정된 계좌의 거래내역을 조회합니다. 최신순으로 정렬됩니다.
**CQRS Query**로 별도 서비스에서 처리합니다.

---

## 1. Endpoint

```
GET /api/accounts/{accountNumber}/activities
```

---

## 2. Request

- Path Variable: `accountNumber` (계좌번호)

---

## 3. Response

```java
public record ActivityResponse(
    Long id,
    ActivityType activityType,
    Long amount,
    Long fee,
    Long balanceAfter,
    String referenceAccountNumber,
    String description,
    LocalDateTime createdAt
) {}

ApiResponse<List<ActivityResponse>>
```

---

## 4. 비즈니스 로직

1. 계좌 존재 확인 → `AccountNotFoundException`
2. 최신순 정렬 (`ORDER BY created_at DESC`)

---

## 5. 시퀀스 다이어그램

```
Client → Controller → QueryService → ActivityRepo → Activity
  │        │            │               │            │
  │─ GET   │            │               │            │
  │        │─ getActivities(accountNumber)          │
  │        │            │─ findByAccountIdOrderByCreatedAtDesc()
  │        │            │───────────────▶│            │
  │        │            │◀───────────────│            │
  │        │            │               │            │
  │        │            │─ map to Response DTO       │
  │        │            │────────────────────────────▶│
  │        │            │◀────────────────────────────│
  │        │            │               │            │
  │◀───────│◀───────────│               │            │
```

---

## 6. Query Service 구현 (CQRS)

```java
@Service
public class GetActivitiesQueryService {
    private final ActivityRepository activityRepository;
    private final AccountRepository accountRepository;

    public GetActivitiesQueryService(ActivityRepository activityRepository,
                                      AccountRepository accountRepository) {
        this.activityRepository = activityRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> execute(String accountNumber) {
        // 계좌 존재 확인
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(AccountNotFoundException::new);

        // 거래내역 조회 (최신순)
        List<Activity> activities = activityRepository
            .findByAccountIdOrderByCreatedAtDesc(account.getId());

        // Response DTO 변환
        return activities.stream()
            .map(ActivityResponse::from)
            .toList();
    }
}
```

---

## 7. Controller 구현

```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final GetActivitiesQueryService getActivitiesQueryService;

    public AccountController(GetActivitiesQueryService getActivitiesQueryService) {
        this.getActivitiesQueryService = getActivitiesQueryService;
    }

    @GetMapping("/{accountNumber}/activities")
    public ApiResponse<List<ActivityResponse>> getActivities(
        @PathVariable String accountNumber
    ) {
        List<ActivityResponse> activities = getActivitiesQueryService.execute(accountNumber);
        return ApiResponse.success(activities);
    }
}
```

---

## 8. ActivityRepository Port

```java
public interface ActivityRepository {
    Activity save(Activity activity);
    List<Activity> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
```

---

## 9. ActivityResponse.from()

```java
public record ActivityResponse(
    Long id,
    ActivityType activityType,
    Long amount,
    Long fee,
    Long balanceAfter,
    String referenceAccountNumber,
    String description,
    LocalDateTime createdAt
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
            activity.getId(),
            activity.getActivityType(),
            activity.getAmount(),
            activity.getFee(),
            activity.getBalanceAfter(),
            activity.getReferenceAccountNumber(),
            activity.getDescription(),
            activity.getCreatedAt()
        );
    }
}
```

---

## 10. CQRS 패턴

| 구분 | Command | Query |
|------|---------|-------|
| **위치** | `account/service` | `activity/service` |
| **용도** | 상태 변경 (입금, 출금, 이체) | 거래내역 조회 |
| **서비스명** | XxxUseCase | XxxQueryService |
| **트랜잭션** | `@Transactional` | `@Transactional(readOnly = true)` |

---

## 11. 관련 파일

| 파일 | 경로 |
|------|------|
| Activity Entity | `activity/src/main/java/com/leesuchan/activity/domain/model/Activity.java` |
| ActivityRepository Port | `activity/src/main/java/com/leesuchan/activity/domain/repository/ActivityRepository.java` |
| GetActivitiesQueryService | `activity/src/main/java/com/leesuchan/activity/service/GetActivitiesQueryService.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |

---

## 12. 예외 처리

| 예외 상황 | 예외 클래스 | 에러 코드 |
|----------|------------|----------|
| 계좌 미조회 | `AccountNotFoundException` | ACCOUNT_001 |

---

## 13. 테스트 작성 참고

테스트 작성 가이드라인은 [01-common.md](./01-common.md#6-테스트-작성-가이드라인)을 참고하세요.

| 테스트 파일 | 경로 |
|-------------|------|
| GetActivitiesQueryServiceTest | `activity/src/test/java/com/leesuchan/activity/service/GetActivitiesQueryServiceTest.java` |
| GetActivitiesE2ETest | `service/src/test/java/com/leesuchan/service/GetActivitiesE2ETest.java` |
