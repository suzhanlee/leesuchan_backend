# API 3: 입금

특정 계좌에 금액을 입금합니다. 한도 제한은 없습니다.

---

## 1. Endpoint

```
POST /api/accounts/deposit
```

---

## 2. Request

```java
public record DepositRequest(
    @NotBlank(message = "계좌번호는 필수입니다.")
    String accountNumber,

    @Positive(message = "금액은 0보다 커야 합니다.")
    Long amount
) {}
```

---

## 3. Response

```java
ApiResponse<AccountResponse>
```

---

## 4. 비즈니스 로직

1. 계좌 존재 확인 → `AccountNotFoundException`
2. 금액 양수 검증 → `IllegalArgumentException`
3. 잔액 증가
4. Activity 기록 (`DEPOSIT`)

---

## 5. 도메인 로직

### Account.deposit()

```java
public void deposit(long amount) {
    if (amount <= 0) {
        throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
    }
    this.balance += amount;
    this.updatedAt = LocalDateTime.now();
}
```

---

## 6. 시퀀스 다이어그램

```
Client → Controller → UseCase → AccountRepo → Account → ActivityRepo
  │        │           │          │            │          │
  │─ POST  │           │          │            │          │
  │        │─ execute()│          │            │          │
  │        │           │─ findByAccountNumber()
  │        │           │───────────▶│          │          │
  │        │           │◀───────────│          │          │
  │        │           │          │            │          │
  │        │           │          │ .deposit()│          │
  │        │           │───────────▶───────────▶│          │
  │        │           │◀───────────◀───────────│          │
  │        │           │          │            │          │
  │        │           │─ save()  │            │          │
  │        │           │───────────▶│          │          │
  │        │           │◀───────────│          │          │
  │        │           │          │            │          │
  │        │           │          │            │─ recordDeposit()
  │        │           │───────────▶───────────▶──────────▶│
  │        │           │◀───────────◀───────────◀───────────│
  │        │           │          │            │          │
  │        │◀──────────│          │            │          │
  │◀───────│           │          │            │          │
```

---

## 7. UseCase 구현

```java
@Service
public class DepositMoneyUseCase {
    private final AccountRepository accountRepository;
    private final ActivityRecordService activityRecordService;

    public DepositMoneyUseCase(AccountRepository accountRepository,
                                ActivityRecordService activityRecordService) {
        this.accountRepository = accountRepository;
        this.activityRecordService = activityRecordService;
    }

    @Transactional
    public Account execute(DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.accountNumber())
            .orElseThrow(AccountNotFoundException::new);

        account.deposit(request.amount());
        accountRepository.save(account);

        activityRecordService.recordDeposit(
            account.getId(),
            request.amount(),
            account.getBalance()
        );

        return account;
    }
}
```

---

## 8. Controller 구현

```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final DepositMoneyUseCase depositMoneyUseCase;

    public AccountController(DepositMoneyUseCase depositMoneyUseCase) {
        this.depositMoneyUseCase = depositMoneyUseCase;
    }

    @PostMapping("/deposit")
    public ApiResponse<AccountResponse> deposit(
        @Valid @RequestBody DepositRequest request
    ) {
        Account account = depositMoneyUseCase.execute(request);
        return ApiResponse.success(AccountResponse.from(account));
    }
}
```

---

## 9. Activity 기록

### ActivityRecordService.recordDeposit()

```java
@Service
public class ActivityRecordService {
    private final ActivityRepository activityRepository;

    @Transactional
    public void recordDeposit(Long accountId, Long amount, Long balanceAfter) {
        Activity activity = Activity.deposit(accountId, amount, balanceAfter);
        activityRepository.save(activity);
    }
}
```

### Activity.deposit() 팩토리 메서드

```java
public static Activity deposit(Long accountId, Long amount, Long balanceAfter) {
    return new Activity(
        accountId,
        ActivityType.DEPOSIT,
        amount,
        0L,  // fee = 0
        balanceAfter,
        null,  // referenceAccountId
        null,  // referenceAccountNumber
        null,  // description
        null   // transactionId
    );
}
```

---

## 10. 관련 파일

| 파일 | 경로 |
|------|------|
| Account Entity | `account/src/main/java/com/leesuchan/account/domain/model/Account.java` |
| AccountRepository Port | `account/src/main/java/com/leesuchan/account/domain/repository/AccountRepository.java` |
| DepositMoneyUseCase | `account/src/main/java/com/leesuchan/account/service/DepositMoneyUseCase.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |
| Activity Entity | `activity/src/main/java/com/leesuchan/activity/domain/model/Activity.java` |
| ActivityRecordService | `activity/src/main/java/com/leesuchan/activity/service/ActivityRecordService.java` |

---

## 11. 예외 처리

| 예외 상황 | 예외 클래스 | 에러 코드 |
|----------|------------|----------|
| 계좌 미조회 | `AccountNotFoundException` | ACCOUNT_001 |
| 금액이 0 이하 | `IllegalArgumentException` | - |

---

## 12. 테스트 작성 참고

테스트 작성 가이드라인은 [01-common.md](./01-common.md#6-테스트-작성-가이드라인)을 참고하세요.

| 테스트 파일 | 경로 |
|-------------|------|
| DepositMoneyUseCaseTest | `account/src/test/java/com/leesuchan/account/service/DepositMoneyUseCaseTest.java` |
| DepositMoneyE2ETest | `service/src/test/java/com/leesuchan/service/DepositMoneyE2ETest.java` |
