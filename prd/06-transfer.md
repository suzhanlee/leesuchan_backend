# API 5: 이체

한 계좌에서 다른 계좌로 금액을 이체합니다.
- **일일 한도**: 3,000,000원
- **수수료**: 이체 금액의 1% (출금 계좌에서 차감)
- **Activity 2개 생성**: `TRANSFER_OUT`, `TRANSFER_IN` (같은 transactionId 연결)

---

## 1. Endpoint

```
POST /api/accounts/transfer
```

---

## 2. Request

```java
public record TransferRequest(
    @NotBlank(message = "출금 계좌번호는 필수입니다.")
    String fromAccountNumber,

    @NotBlank(message = "입금 계좌번호는 필수입니다.")
    String toAccountNumber,

    @Positive(message = "금액은 0보다 커야 합니다.")
    Long amount
) {}
```

---

## 3. Response

```java
public record TransferResponse(
    AccountResponse fromAccount,
    AccountResponse toAccount,
    Long fee
) {}

ApiResponse<TransferResponse>
```

---

## 4. 비즈니스 로직

1. 두 계좌 존재 확인 → `AccountNotFoundException`
2. 동일 계좌 이체 불가 → `SameAccountTransferException`
3. 금액 양수 검증 → `IllegalArgumentException`
4. **일일 한도 체크**: 3,000,000원 초과 시 → `DailyTransferLimitExceededException`
5. **수수료 계산**: `amount / 100`
6. 출금 계좌 잔액 확인 (금액 + 수수료) → `InsufficientBalanceException`
7. **@Transactional**로 원자성 보장
8. **Activity 2개 생성** (같은 transactionId로 연결)

---

## 5. 수수료 계산

```java
long fee = amount / 100;  // 1%
```

---

## 6. 도메인 로직

### Account.transfer()

```java
public void transfer(Account to, long amount) {
    if (amount <= 0) {
        throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
    }
    checkDailyTransferLimit(amount);

    long fee = calculateFee(amount);
    long totalAmount = amount + fee;
    checkSufficientBalance(totalAmount);

    // 출금
    this.balance -= totalAmount;
    this.dailyTransferAmount += amount;
    this.lastTransferDate = LocalDate.now();

    // 입금
    to.balance += amount;

    this.updatedAt = LocalDateTime.now();
}

private void checkDailyTransferLimit(long amount) {
    LocalDate today = LocalDate.now();
    if (this.lastTransferDate == null || !this.lastTransferDate.equals(today)) {
        this.dailyTransferAmount = 0L;
        this.lastTransferDate = today;
    }
    if (this.dailyTransferAmount + amount > 3_000_000L) {
        throw new DailyTransferLimitExceededException();
    }
}

private long calculateFee(long amount) {
    return amount / 100;
}
```

---

## 7. 시퀀스 다이어그램

```
Client → Controller → UseCase → AccountRepo → ActivityRepo
  │        │           │          │            │
  │─ POST  │           │          │            │
  │        │─ execute(from, to, amount)       │
  │        │           │          │            │
  │        │           │─ findByAccountNumber(from)
  │        │           │───────────▶│          │
  │        │           │◀───────────│          │
  │        │           │          │            │
  │        │           │─ findByAccountNumber(to)
  │        │           │───────────▶│          │
  │        │           │◀───────────│          │
  │        │           │          │            │
  │        │           │─ @Transactional      │
  │        │           │  from.transfer(to)   │
  │        │           │───────────▶│          │
  │        │           │  [수수료 계산 1%]     │
  │        │           │  [한도 체크 300만]    │
  │        │           │  from.balance -= amount+fee
  │        │           │  to.balance += amount
  │        │           │◀───────────│          │
  │        │           │          │            │
  │        │           │─ saveAll(from, to)   │
  │        │           │───────────▶│          │
  │        │           │◀───────────│          │
  │        │           │          │            │
  │        │           │─ recordTransferOut() ────▶ Activity 1 생성
  │        │           │─────────────────────────────────▶│
  │        │           │◀─────────────────────────────────│
  │        │           │          │            │
  │        │           │─ recordTransferIn() ─────▶ Activity 2 생성
  │        │           │─────────────────────────────────▶│
  │        │           │◀─────────────────────────────────│
  │        │           │          │            │
  │        │◀──────────│          │            │
  │◀───────│           │          │            │
```

---

## 8. UseCase 구현

```java
@Service
public class TransferMoneyUseCase {
    private final AccountRepository accountRepository;
    private final ActivityRecordService activityRecordService;

    public TransferMoneyUseCase(AccountRepository accountRepository,
                                 ActivityRecordService activityRecordService) {
        this.accountRepository = accountRepository;
        this.activityRecordService = activityRecordService;
    }

    @Transactional
    public TransferResult execute(TransferRequest request) {
        // 1. 두 계좌 조회
        Account from = accountRepository.findByAccountNumber(request.fromAccountNumber())
            .orElseThrow(AccountNotFoundException::new);
        Account to = accountRepository.findByAccountNumber(request.toAccountNumber())
            .orElseThrow(AccountNotFoundException::new);

        // 2. 동일 계좌 체크
        if (from.getId().equals(to.getId())) {
            throw new SameAccountTransferException();
        }

        // 3. 이체 수행
        from.transfer(to, request.amount());

        // 4. 저장
        accountRepository.saveAll(List.of(from, to));

        // 5. 수수료 계산
        long fee = request.amount() / 100;

        // 6. Activity 기록 (2개)
        String transactionId = generateTransactionId();
        activityRecordService.recordTransferOut(
            from.getId(),
            to.getId(),
            to.getAccountNumber(),
            request.amount(),
            fee,
            from.getBalance(),
            transactionId
        );
        activityRecordService.recordTransferIn(
            to.getId(),
            from.getId(),
            from.getAccountNumber(),
            request.amount(),
            to.getBalance(),
            transactionId
        );

        return new TransferResult(from, to, fee);
    }

    private String generateTransactionId() {
        return "TX_" + System.currentTimeMillis();
    }
}
```

---

## 9. Controller 구현

```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final TransferMoneyUseCase transferMoneyUseCase;

    public AccountController(TransferMoneyUseCase transferMoneyUseCase) {
        this.transferMoneyUseCase = transferMoneyUseCase;
    }

    @PostMapping("/transfer")
    public ApiResponse<TransferResponse> transfer(
        @Valid @RequestBody TransferRequest request
    ) {
        TransferResult result = transferMoneyUseCase.execute(request);
        return ApiResponse.success(new TransferResponse(
            AccountResponse.from(result.from()),
            AccountResponse.from(result.to()),
            result.fee()
        ));
    }
}
```

---

## 10. Activity 생성 (2개)

### Activity 1: TRANSFER_OUT (출금 계좌)

```java
Activity.transferOut(
    fromAccountId,
    toAccountId,
    toAccountNumber,
    amount,      // 100,000
    fee,         // 1,000 (1%)
    balanceAfter,// 899,000
    transactionId
)
```

### Activity 2: TRANSFER_IN (입금 계좌)

```java
Activity.transferIn(
    toAccountId,
    fromAccountId,
    fromAccountNumber,
    amount,      // 100,000
    0L,          // fee = 0
    balanceAfter,// 100,000
    transactionId  // 같은 ID로 연결
)
```

### ActivityRecordService

```java
@Service
public class ActivityRecordService {
    private final ActivityRepository activityRepository;

    @Transactional
    public void recordTransferOut(
        Long fromAccountId,
        Long toAccountId,
        String toAccountNumber,
        Long amount,
        Long fee,
        Long balanceAfter,
        String transactionId
    ) {
        Activity activity = Activity.transferOut(
            fromAccountId, toAccountId, toAccountNumber,
            amount, fee, balanceAfter, transactionId
        );
        activityRepository.save(activity);
    }

    @Transactional
    public void recordTransferIn(
        Long toAccountId,
        Long fromAccountId,
        String fromAccountNumber,
        Long amount,
        Long balanceAfter,
        String transactionId
    ) {
        Activity activity = Activity.transferIn(
            toAccountId, fromAccountId, fromAccountNumber,
            amount, balanceAfter, transactionId
        );
        activityRepository.save(activity);
    }
}
```

### Activity 팩토리 메서드

```java
public static Activity transferOut(
    Long accountId,
    Long referenceAccountId,
    String referenceAccountNumber,
    Long amount,
    Long fee,
    Long balanceAfter,
    String transactionId
) {
    return new Activity(
        accountId,
        ActivityType.TRANSFER_OUT,
        amount,
        fee,
        balanceAfter,
        referenceAccountId,
        referenceAccountNumber,
        null,  // description
        transactionId
    );
}

public static Activity transferIn(
    Long accountId,
    Long referenceAccountId,
    String referenceAccountNumber,
    Long amount,
    Long balanceAfter,
    String transactionId
) {
    return new Activity(
        accountId,
        ActivityType.TRANSFER_IN,
        amount,
        0L,  // fee = 0
        balanceAfter,
        referenceAccountId,
        referenceAccountNumber,
        null,  // description
        transactionId
    );
}
```

---

## 11. 관련 파일

| 파일 | 경로 |
|------|------|
| Account Entity | `account/src/main/java/com/leesuchan/account/domain/model/Account.java` |
| AccountRepository Port | `account/src/main/java/com/leesuchan/account/domain/repository/AccountRepository.java` |
| TransferMoneyUseCase | `account/src/main/java/com/leesuchan/account/service/TransferMoneyUseCase.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |
| Activity Entity | `activity/src/main/java/com/leesuchan/activity/domain/model/Activity.java` |
| ActivityRecordService | `activity/src/main/java/com/leesuchan/activity/service/ActivityRecordService.java` |

---

## 12. 예외 처리

| 예외 상황 | 예외 클래스 | 에러 코드 |
|----------|------------|----------|
| 출금 계좌 미조회 | `AccountNotFoundException` | ACCOUNT_001 |
| 입금 계좌 미조회 | `AccountNotFoundException` | ACCOUNT_001 |
| 동일 계좌 이체 | `SameAccountTransferException` | ACCOUNT_007 |
| 금액이 0 이하 | `IllegalArgumentException` | - |
| 일일 한도 초과 (3,000,000원) | `DailyTransferLimitExceededException` | ACCOUNT_006 |
| 잔액 부족 (금액 + 수수료) | `InsufficientBalanceException` | ACCOUNT_004 |

---

## 13. 테스트 작성 참고

테스트 작성 가이드라인은 [01-common.md](./01-common.md#6-테스트-작성-가이드라인)을 참고하세요.

| 테스트 파일 | 경로 |
|-------------|------|
| TransferMoneyUseCaseTest | `account/src/test/java/com/leesuchan/account/service/TransferMoneyUseCaseTest.java` |
| TransferMoneyE2ETest | `service/src/test/java/com/leesuchan/service/TransferMoneyE2ETest.java` |
