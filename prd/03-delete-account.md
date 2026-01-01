# API 2: 계좌 삭제

기존 계좌를 삭제합니다. 실제 삭제가 아닌 소프트 삭제(deletedAt 설정)를 수행합니다.

---

## 1. Endpoint

```
DELETE /api/accounts/{accountNumber}
```

---

## 2. Request

- Path Variable: `accountNumber` (계좌번호)

---

## 3. Response

```java
ApiResponse<Void>
```

---

## 4. 비즈니스 로직

1. 계좌 존재 확인 → `AccountNotFoundException`
2. 소프트 삭제 수행 (deletedAt = now)

---

## 5. 도메인 로직

### Account.delete()

```java
public void delete() {
    this.deletedAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
}

public boolean isDeleted() {
    return deletedAt != null;
}
```

---

## 6. 시퀀스 다이어그램

```
Client → Controller → UseCase → AccountRepo → Account
  │        │           │          │            │
  │─ DELETE│           │          │            │
  │        │─ execute(accountNumber)           │
  │        │           │          │            │
  │        │           │─ findByAccountNumber()
  │        │           │───────────▶│          │
  │        │           │◀───────────│          │
  │        │           │          │            │
  │        │           │          │ .delete() │
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
public class DeleteAccountUseCase {
    private final AccountRepository accountRepository;

    public DeleteAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void execute(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(AccountNotFoundException::new);
        account.delete();
        accountRepository.save(account);
    }
}
```

---

## 8. Controller 구현

```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final DeleteAccountUseCase deleteAccountUseCase;

    public AccountController(DeleteAccountUseCase deleteAccountUseCase) {
        this.deleteAccountUseCase = deleteAccountUseCase;
    }

    @DeleteMapping("/{accountNumber}")
    public ApiResponse<Void> delete(@PathVariable String accountNumber) {
        deleteAccountUseCase.execute(accountNumber);
        return ApiResponse.success();
    }
}
```

---

## 9. 관련 파일

| 파일 | 경로 |
|------|------|
| Account Entity | `account/src/main/java/com/leesuchan/account/domain/model/Account.java` |
| AccountRepository Port | `account/src/main/java/com/leesuchan/account/domain/repository/AccountRepository.java` |
| DeleteAccountUseCase | `account/src/main/java/com/leesuchan/account/service/DeleteAccountUseCase.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |
| AccountNotFoundException | `account/src/main/java/com/leesuchan/account/domain/exception/AccountNotFoundException.java` |

---

## 10. 예외 처리

| 예외 상황 | 예외 클래스 | 에러 코드 |
|----------|------------|----------|
| 계좌 미조회 | `AccountNotFoundException` | ACCOUNT_001 |

---

## 11. 테스트 작성 참고

테스트 작성 가이드라인은 [01-common.md](./01-common.md#6-테스트-작성-가이드라인)을 참고하세요.

| 테스트 파일 | 경로 |
|-------------|------|
| DeleteAccountUseCaseTest | `account/src/test/java/com/leesuchan/account/service/DeleteAccountUseCaseTest.java` |
| DeleteAccountE2ETest | `service/src/test/java/com/leesuchan/service/DeleteAccountE2ETest.java` |
