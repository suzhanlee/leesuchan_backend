# API 4: 출금

특정 계좌에서 금액을 출금합니다. **일일 한도 1,000,000원**이 적용됩니다.

---

## 1. Endpoint

```
POST /api/accounts/withdraw
```

---

## 2. Request

```java
public record WithdrawRequest(
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
3. **일일 한도 체크**: 1,000,000원 초과 시 → `DailyWithdrawLimitExceededException`
4. 잔액 확인 → `InsufficientBalanceException`
5. 잔액 차감
6. Activity 기록 (`WITHDRAW`)

---

## 5. 한도 체크 로직

```java
// 날짜가 바뀌면 한도 리셋
if (!isToday(lastWithdrawDate)) {
    dailyWithdrawAmount = 0L;
}
// 누적 + 현재 금액이 한도 초과 시 예외
if (dailyWithdrawAmount + amount > 1_000_000L) {
    throw new DailyWithdrawLimitExceededException();
}
```

---

## 6. 도메인 로직

### Account.withdraw()

```java
public void withdraw(long amount) {
    if (amount <= 0) {
        throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
    }
    checkDailyWithdrawLimit(amount);
    checkSufficientBalance(amount);

    this.balance -= amount;
    this.dailyWithdrawAmount += amount;
    this.lastWithdrawDate = LocalDate.now();
    this.updatedAt = LocalDateTime.now();
}

private void checkDailyWithdrawLimit(long amount) {
    LocalDate today = LocalDate.now();
    if (this.lastWithdrawDate == null || !this.lastWithdrawDate.equals(today)) {
        this.dailyWithdrawAmount = 0L;
        this.lastWithdrawDate = today;
    }
    if (this.dailyWithdrawAmount + amount > 1_000_000L) {
        throw new DailyWithdrawLimitExceededException();
    }
}

private void checkSufficientBalance(long amount) {
    if (this.balance < amount) {
        throw new InsufficientBalanceException();
    }
}
```

---

## 7. 시퀀스 다이어그램

```
Client → Controller → UseCase → AccountRepo → Account → ActivityRepo
  │        │           │          │            │          │
  │─ POST  │           │          │            │          │
  │        │─ execute()│          │            │          │
  │        │           │─ findByAccountNumber()
  │        │           │───────────▶│          │          │
  │        │           │◀───────────│          │          │
  │        │           │          │            │          │
  │        │           │          │ .withdraw()│         │
  │        │           │───────────▶───────────▶│         │
  │        │           │          │  [한도체크] │          │
  │        │           │          │  [잔액체크] │          │
  │        │           │◀───────────◀───────────│          │
  │        │           │          │            │          │
  │        │           │─ save()  │            │          │
  │        │           │───────────▶│          │          │
  │        │           │◀───────────│          │          │
  │        │           │          │            │          │
  │        │           │          │            │─ recordWithdraw()
  │        │           │───────────▶───────────▶──────────▶│
  │        │           │◀───────────◀───────────◀───────────│
  │        │           │          │            │          │
  │        │◀──────────│          │            │          │
  │◀───────│           │          │            │          │
```

---

## 8. UseCase 구현

```java
@Service
public class WithdrawMoneyUseCase {
    private final AccountRepository accountRepository;
    private final ActivityRecordService activityRecordService;

    public WithdrawMoneyUseCase(AccountRepository accountRepository,
                                 ActivityRecordService activityRecordService) {
        this.accountRepository = accountRepository;
        this.activityRecordService = activityRecordService;
    }

    @Transactional
    public Account execute(WithdrawRequest request) {
        Account account = accountRepository.findByAccountNumber(request.accountNumber())
            .orElseThrow(AccountNotFoundException::new);

        account.withdraw(request.amount());
        accountRepository.save(account);

        activityRecordService.recordWithdraw(
            account.getId(),
            request.amount(),
            account.getBalance()
        );

        return account;
    }
}
```

---

## 9. Controller 구현

```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;

    public AccountController(WithdrawMoneyUseCase withdrawMoneyUseCase) {
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
    }

    @PostMapping("/withdraw")
    public ApiResponse<AccountResponse> withdraw(
        @Valid @RequestBody WithdrawRequest request
    ) {
        Account account = withdrawMoneyUseCase.execute(request);
        return ApiResponse.success(AccountResponse.from(account));
    }
}
```

---

## 10. Activity 기록

### ActivityRecordService.recordWithdraw()

```java
@Transactional
public void recordWithdraw(Long accountId, Long amount, Long balanceAfter) {
    Activity activity = Activity.withdraw(accountId, amount, balanceAfter);
    activityRepository.save(activity);
}
```

### Activity.withdraw() 팩토리 메서드

```java
public static Activity withdraw(Long accountId, Long amount, Long balanceAfter) {
    return new Activity(
        accountId,
        ActivityType.WITHDRAW,
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

## 11. 관련 파일

| 파일 | 경로 |
|------|------|
| Account Entity | `account/src/main/java/com/leesuchan/account/domain/model/Account.java` |
| AccountRepository Port | `account/src/main/java/com/leesuchan/account/domain/repository/AccountRepository.java` |
| WithdrawMoneyUseCase | `account/src/main/java/com/leesuchan/account/service/WithdrawMoneyUseCase.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |
| Activity Entity | `activity/src/main/java/com/leesuchan/activity/domain/model/Activity.java` |
| ActivityRecordService | `activity/src/main/java/com/leesuchan/activity/service/ActivityRecordService.java` |

---

## 12. 예외 처리

| 예외 상황 | 예외 클래스 | 에러 코드 |
|----------|------------|----------|
| 계좌 미조회 | `AccountNotFoundException` | ACCOUNT_001 |
| 금액이 0 이하 | `IllegalArgumentException` | - |
| 일일 한도 초과 (1,000,000원) | `DailyWithdrawLimitExceededException` | ACCOUNT_005 |
| 잔액 부족 | `InsufficientBalanceException` | ACCOUNT_004 |

---

## 13. 테스트 작성 참고

테스트 작성 가이드라인은 [01-common.md](./01-common.md#6-테스트-작성-가이드라인)을 참고하세요.

| 테스트 파일 | 경로 |
|-------------|------|
| WithdrawMoneyUseCaseTest | `account/src/test/java/com/leesuchan/account/service/WithdrawMoneyUseCaseTest.java` |
| WithdrawMoneyE2ETest | `service/src/test/java/com/leesuchan/service/WithdrawMoneyE2ETest.java` |
