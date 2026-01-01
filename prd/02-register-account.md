# API 1: 계좌 등록

새로운 계좌를 등록합니다. 초기 잔액은 0원으로 시작합니다.

---

## 1. Endpoint

```
POST /api/accounts
```

---

## 2. Request

```java
public record RegisterAccountRequest(
    @NotBlank(message = "계좌번호는 필수입니다.")
    String accountNumber,

    @NotBlank(message = "계좌명은 필수입니다.")
    String accountName
) {}
```

---

## 3. Response

```java
public record AccountResponse(
    Long id,
    String accountNumber,
    String accountName,
    Long balance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getAccountName(),
            account.getBalance(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}
```

---

## 4. 비즈니스 로직

1. 계좌번호 중복 체크 → `DuplicateAccountException`
2. 계좌명 유효성 검증 (null, blank, 길이 100자 초과) → `InvalidAccountNameException`
3. 초기 잔액 0원으로 계좌 생성

---

## 5. 도메인 로직

### Account.create()

```java
public static Account create(String accountNumber, String accountName) {
    return new Account(accountNumber, accountName, 0L);
}
```

### Account 생성자 (패키지 private)

```java
Account(String accountNumber, String accountName, Long balance) {
    validateAccountNumber(accountNumber);
    if (accountName == null || accountName.isBlank()) {
        throw new InvalidAccountNameException("계좌명은 비어있을 수 없습니다.");
    }
    if (accountName.length() > 100) {
        throw new InvalidAccountNameException("계좌명은 100자 이하여야 합니다.");
    }
    this.accountNumber = accountNumber;
    this.accountName = accountName;
    this.balance = balance;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.deletedAt = null;
}
```

---

## 6. 시퀀스 다이어그램

```
Client → Controller → UseCase → AccountRepo → Account
  │        │           │          │            │
  │─ POST  │           │          │            │
  │        │─ execute()│          │            │
  │        │           │          │            │
  │        │           │─ existsByAccountNumber()
  │        │           │───────────▶│          │
  │        │           │◀───────────│          │
  │        │           │          │            │
  │        │           │─ Account.create()    │
  │        │           │──────────────────────▶│
  │        │           │◀──────────────────────│
  │        │           │          │            │
  │        │           │─ save()  │            │
  │        │           │───────────▶│          │
  │        │           │◀───────────│          │
  │        │           │          │            │
  │        │◀──────────│          │            │
  │◀───────│           │          │            │
```

---

## 7. UseCase 구현

```java
@Service
public class RegisterAccountUseCase {
    private final AccountRepository accountRepository;

    public RegisterAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account execute(RegisterAccountRequest request) {
        // 중복 체크
        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new DuplicateAccountException();
        }
        // 생성
        Account account = Account.create(
            request.accountNumber(),
            request.accountName()
        );
        return accountRepository.save(account);
    }
}
```

---

## 8. Controller 구현

```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final RegisterAccountUseCase registerAccountUseCase;

    public AccountController(RegisterAccountUseCase registerAccountUseCase) {
        this.registerAccountUseCase = registerAccountUseCase;
    }

    @PostMapping
    public ApiResponse<AccountResponse> register(
        @Valid @RequestBody RegisterAccountRequest request
    ) {
        Account account = registerAccountUseCase.execute(request);
        return ApiResponse.success(AccountResponse.from(account));
    }
}
```

---

## 9. 관련 파일

| 파일 | 경로 |
|------|------|
| Account Entity | `account/src/main/java/com/leesuchan/account/domain/model/Account.java` |
| AccountRepository Port | `account/src/main/java/com/leesuchan/account/domain/repository/AccountRepository.java` |
| RegisterAccountUseCase | `account/src/main/java/com/leesuchan/account/service/RegisterAccountUseCase.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |
| DuplicateAccountException | `account/src/main/java/com/leesuchan/account/domain/exception/DuplicateAccountException.java` |
| InvalidAccountNameException | `account/src/main/java/com/leesuchan/account/domain/exception/InvalidAccountNameException.java` |

---

## 10. 예외 처리

| 예외 상황 | 예외 클래스 | 에러 코드 |
|----------|------------|----------|
| 계좌번호 중복 | `DuplicateAccountException` | ACCOUNT_002 |
| 계좌명이 null 또는 blank | `InvalidAccountNameException` | ACCOUNT_003 |
| 계좌명이 100자 초과 | `InvalidAccountNameException` | ACCOUNT_003 |

---

## 11. 테스트 작성 참고

테스트 작성 가이드라인은 [01-common.md](./01-common.md#6-테스트-작성-가이드라인)을 참고하세요.

| 테스트 파일 | 경로 |
|-------------|------|
| RegisterAccountUseCaseTest | `account/src/test/java/com/leesuchan/account/service/RegisterAccountUseCaseTest.java` |
| RegisterAccountE2ETest | `service/src/test/java/com/leesuchan/service/RegisterAccountE2ETest.java` |
