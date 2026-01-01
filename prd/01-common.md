# 공통 사항

송금 서비스의 공통 Entity 구조, 도메인 예외, 코딩 컨벤션을 정의합니다.

---

## 1. Entity 구조

### 1.1 Account Entity (계좌)

**테이블**: `account`

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 식별자 |
| account_number | VARCHAR(20) | UNIQUE, NOT NULL | 계좌번호 |
| account_name | VARCHAR(100) | NOT NULL | 계좌명 |
| balance | BIGINT | NOT NULL, DEFAULT 0 | 잔액 (원 단위) |
| daily_withdraw_amount | BIGINT | NOT NULL, DEFAULT 0 | 일일 출금 누적액 |
| daily_transfer_amount | BIGINT | NOT NULL, DEFAULT 0 | 일일 이체 누적액 |
| last_withdraw_date | DATE | NULL | 마지막 출금일 |
| last_transfer_date | DATE | NULL | 마지막 이체일 |
| created_at | DATETIME | NOT NULL | 생성일시 |
| updated_at | DATETIME | NOT NULL | 수정일시 |
| deleted_at | DATETIME | NULL | 소프트 삭제일시 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 낙관적 락 버전 |

### 1.2 Activity Entity (거래내역)

**테이블**: `activity`

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 식별자 |
| account_id | BIGINT | FK, NOT NULL | 계좌 ID |
| activity_type | VARCHAR(20) | NOT NULL | 거래 유형 |
| amount | BIGINT | NOT NULL | 거래 금액 |
| fee | BIGINT | NOT NULL, DEFAULT 0 | 수수료 |
| balance_after | BIGINT | NOT NULL | 거래 후 잔액 |
| reference_account_id | BIGINT | NULL | 이체 시 상대방 계좌 ID |
| reference_account_number | VARCHAR(20) | NULL | 이체 시 상대방 계좌번호 |
| description | VARCHAR(200) | NULL | 메모 |
| transaction_id | VARCHAR(50) | NULL | 트랜잭션 그룹 ID (이체 시 입출금 쌍 연결) |
| created_at | DATETIME | NOT NULL | 생성일시 |

**ActivityType enum**:
```java
public enum ActivityType {
    DEPOSIT,        // 입금
    WITHDRAW,       // 출금
    TRANSFER_OUT,   // 이체 (출금)
    TRANSFER_IN     // 이체 (입금)
}
```

---

## 2. 도메인 예외

### 2.1 AccountErrorCode (에러 코드 인터페이스)

```java
public interface AccountErrorCode extends ErrorCode {
    ErrorCode NOT_FOUND = of("ACCOUNT_001", "계좌를 찾을 수 없습니다.");
    ErrorCode DUPLICATE = of("ACCOUNT_002", "이미 존재하는 계좌번호입니다.");
    ErrorCode INVALID_NAME = of("ACCOUNT_003", "계좌명이 유효하지 않습니다.");
    ErrorCode INSUFFICIENT_BALANCE = of("ACCOUNT_004", "잔액이 부족합니다.");
    ErrorCode DAILY_WITHDRAW_LIMIT_EXCEEDED = of("ACCOUNT_005", "일일 출금 한도를 초과했습니다. (1,000,000원)");
    ErrorCode DAILY_TRANSFER_LIMIT_EXCEEDED = of("ACCOUNT_006", "일일 이체 한도를 초과했습니다. (3,000,000원)");
    ErrorCode SAME_ACCOUNT_TRANSFER = of("ACCOUNT_007", "동일 계좌로 이체할 수 없습니다.");
}
```

### 2.2 도메인 예외 클래스

| 예외 클래스 | 에러 코드 | 사용 시점 |
|------------|----------|-----------|
| `AccountNotFoundException` | ACCOUNT_001 | 계좌 미조회 시 |
| `DuplicateAccountException` | ACCOUNT_002 | 계좌번호 중복 시 |
| `InvalidAccountNameException` | ACCOUNT_003 | 계좌명 유효성 실패 |
| `InsufficientBalanceException` | ACCOUNT_004 | 잔액 부족 시 |
| `DailyWithdrawLimitExceededException` | ACCOUNT_005 | 출금 한도 초과 |
| `DailyTransferLimitExceededException` | ACCOUNT_006 | 이체 한도 초과 |
| `SameAccountTransferException` | ACCOUNT_007 | 동일 계좌 이체 시 |

**예외 클래스 패턴**:
```java
public class DailyWithdrawLimitExceededException extends DomainException {
    public DailyWithdrawLimitExceededException() {
        super(AccountErrorCode.DAILY_WITHDRAW_LIMIT_EXCEEDED);
    }
}
```

---

## 3. 코딩 컨벤션 (CLAUDE.md 준수)

### 3.1 빈 등록 (Bean Registration)

```java
@Service
public class DepositMoneyUseCase {
    private final AccountRepository accountRepository;

    // 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
    public DepositMoneyUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
}
```
- `@Service` 어노테이션 사용
- 명시적 생성자 주입
- 생성자가 하나인 경우 `@Autowired` 생략 가능

### 3.2 Entity

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {
    private Long balance;

    // ❌ 금지: setter 사용
    // public void setBalance(Long balance) { ... }

    // ✅ 올바른: 도메인 메서드로 상태 변경
    public void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
    }
}
```
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 사용
- Setter 금지
- 상태 변경은 도메인 메서드로만 (`deposit()`, `withdraw()` 등)

### 3.3 DTO (record vs class)

**record 사용 (DTO)**:
```java
// API Request/Response DTO
public record RegisterAccountRequest(
    @NotBlank(message = "계좌번호는 필수입니다.")
    String accountNumber,
    @NotBlank(message = "계좌명은 필수입니다.")
    String accountName
) {}

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
            // ...
        );
    }
}
```
- **불변 데이터 전달 객체**: `record` 사용
- Request/Response DTO, 내부 전달 객체
- record 접근: `request.accountNumber()` (getter 아님)

**class 사용 (Entity, 팩토리)**:
```java
// JPA Entity
@Entity
public class Account {
    // JPA 제약으로 class 사용
}
```
- **JPA Entity**: `class` 사용 (JPA 제약)

---

## 4. 한도 체크 로직 공통

### 4.1 날짜 기반 한도 리셋

```java
private void checkDailyWithdrawLimit(long amount) {
    LocalDate today = LocalDate.now();
    // 날짜가 바뀌면 한도 리셋
    if (this.lastWithdrawDate == null || !this.lastWithdrawDate.equals(today)) {
        this.dailyWithdrawAmount = 0L;
        this.lastWithdrawDate = today;
    }
    // 누적 + 현재 금액이 한도 초과 시 예외
    if (this.dailyWithdrawAmount + amount > 1_000_000L) {
        throw new DailyWithdrawLimitExceededException();
    }
}
```

### 4.2 한도 규칙

| 구분 | 출금 | 이체 |
|------|------|------|
| **일일 한도** | 1,000,000원 | 3,000,000원 |
| **수수료** | 없음 | 이체 금액의 1% |
| **한도 리셋** | 날짜 변경 시 | 날짜 변경 시 |
| **한도 누적 필드** | `dailyWithdrawAmount` | `dailyTransferAmount` |
| **관련 예외** | `DailyWithdrawLimitExceededException` | `DailyTransferLimitExceededException` |

---

## 5. 동시성 처리

### 5.1 낙관적 락 (Optimistic Lock)

Account Entity에 `@Version` 필드 사용:
```java
@Version
private Long version;
```

JPA가 자동으로 버전 충돌을 감지하고, 충돌 시 `OptimisticLockingFailureException` 발생.

### 5.2 재시료 메커니즘

```java
@Retryable(
    value = {OptimisticLockingFailureException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
```

---

## 6. 테스트 작성 가이드라인

### 6.1 UseCase 단위 테스트

UseCase 단위 테스트는 Mockito를 사용하여 의존성을 Mock하고 비즈니스 로직을 검증합니다.

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterAccountUseCase 테스트")
class RegisterAccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    private RegisterAccountUseCase registerAccountUseCase;

    @BeforeEach
    void setUp() {
        registerAccountUseCase = new RegisterAccountUseCase(accountRepository);
    }

    @Test
    @DisplayName("계좌를 등록한다")
    void register_account() {
        // given
        String accountNumber = "1234567890";
        String accountName = "홍길동";
        RegisterAccountRequest request = new RegisterAccountRequest(accountNumber, accountName);

        given(accountRepository.existsByAccountNumber(accountNumber))
            .willReturn(false);
        given(accountRepository.save(any(Account.class)))
            .willReturn(Account.create(accountNumber, accountName));

        // when
        Account account = registerAccountUseCase.execute(request);

        // then
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getAccountName()).isEqualTo(accountName);
        assertThat(account.getBalance()).isEqualTo(0L);

        then(accountRepository).should()
            .existsByAccountNumber(accountNumber);
        then(accountRepository).should()
            .save(any(Account.class));
    }

    @Test
    @DisplayName("이미 존재하는 계좌번호로 등록하면 예외가 발생한다")
    void register_duplicate_account_throws_exception() {
        // given
        String accountNumber = "1234567890";
        String accountName = "홍길동";
        RegisterAccountRequest request = new RegisterAccountRequest(accountNumber, accountName);

        given(accountRepository.existsByAccountNumber(accountNumber))
            .willReturn(true);

        // when & then
        assertThatThrownBy(() -> registerAccountUseCase.execute(request))
                .isInstanceOf(DuplicateAccountException.class);

        then(accountRepository).should()
            .existsByAccountNumber(accountNumber);
        then(accountRepository).should(never())
            .save(any(Account.class));
    }
}
```

**주요 포인트**:
- `@ExtendWith(MockitoExtension.class)` 사용
- `@Mock`으로 의존성 주입
- given-when-then 패턴 사용 (BDDMockito)
- `given()`, `then()`, `should()` 사용
- AssertJ `assertThatThrownBy()`로 예외 검증
- `never()`로 호출되지 않음 검증

### 6.2 Controller E2E 테스트

Controller E2E 테스트는 MockMvc를 사용하여 HTTP 요청/응답을 검증합니다.

```java
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("계좌 등록 E2E 테스트")
class RegisterAccountE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterAccountUseCase registerAccountUseCase;

    @BeforeEach
    void setUp() {
        reset(registerAccountUseCase);
    }

    @Test
    @DisplayName("계좌를 등록하는 API를 호출한다")
    void register_account_api() throws Exception {
        // given
        String accountNumber = "1234567890";
        String accountName = "홍길동";
        Account account = Account.create(accountNumber, accountName);

        given(registerAccountUseCase.execute(any(RegisterAccountRequest.class)))
            .willReturn(account);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "accountNumber": "1234567890",
                        "accountName": "홍길동"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.status.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.data.accountName").value("홍길동"))
                .andExpect(jsonPath("$.data.balance").value(0));

        then(registerAccountUseCase).should()
            .execute(any(RegisterAccountRequest.class));
    }

    @Test
    @DisplayName("계좌번호가 비어있으면 400 에러가 발생한다")
    void register_account_empty_account_number_400() throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "accountNumber": "",
                        "accountName": "홍길동"
                    }
                    """))
                .andExpect(status().isBadRequest());

        then(registerAccountUseCase).should(never())
            .execute(any(RegisterAccountRequest.class));
    }
}
```

**주요 포인트**:
- `@SpringBootTest` + `@AutoConfigureMockMvc` 사용
  - `@WebMvcTest`는 JPA EntityManagerFactory 로드하지 않아 @EnableJpaRepositories와 충돌
- `@MockBean`으로 UseCase Mock 주입
- `MockMvc`로 HTTP 요청/응답 테스트
- `jsonPath()`로 JSON 응답 검증
- `reset()`으로 Mock 초기화

### 6.3 Entity 도메인 테스트

Entity 도메인 테스트는 도메인 로직을 직접 검증합니다.

```java
@DisplayName("Account 도메인 테스트")
class AccountTest {

    @Test
    @DisplayName("계좌를 생성한다")
    void create_account() {
        // when
        Account account = Account.create("1234567890", "홍길동");

        // then
        assertThat(account.getAccountNumber()).isEqualTo("1234567890");
        assertThat(account.getAccountName()).isEqualTo("홍길동");
        assertThat(account.getBalance()).isEqualTo(0L);
    }

    @Test
    @DisplayName("계좌명이 비어있으면 예외가 발생한다")
    void create_account_empty_name_throws_exception() {
        // when & then
        assertThatThrownBy(() -> Account.create("1234567890", ""))
                .isInstanceOf(InvalidAccountNameException.class);
    }

    @Test
    @DisplayName("입금한다")
    void deposit() {
        // given
        Account account = Account.create("1234567890", "홍길동");

        // when
        account.deposit(10000L);

        // then
        assertThat(account.getBalance()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("0원 이하로 입금하면 예외가 발생한다")
    void deposit_zero_or_negative_throws_exception() {
        // given
        Account account = Account.create("1234567890", "홍길동");

        // when & then
        assertThatThrownBy(() -> account.deposit(0L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.deposit(-1000L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("출금한다")
    void withdraw() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        account.deposit(10000L);

        // when
        account.withdraw(3000L);

        // then
        assertThat(account.getBalance()).isEqualTo(7000L);
    }

    @Test
    @DisplayName("잔액보다 많이 출금하면 예외가 발생한다")
    void withdraw_insufficient_balance_throws_exception() {
        // given
        Account account = Account.create("1234567890", "홍길동");
        account.deposit(5000L);

        // when & then
        assertThatThrownBy(() -> account.withdraw(10000L))
                .isInstanceOf(InsufficientBalanceException.class);
    }
}
```

### 6.4 테스트 명명 규칙

| 구분 | 규칙 | 예시 |
|------|------|------|
| 클래스명 | `XxxTest` (단위 테스트) | `RegisterAccountUseCaseTest` |
| 클래스명 | `XxxE2ETest` (E2E 테스트) | `RegisterAccountE2ETest` |
| 메서드명 | snake_case, 한국어 @DisplayName 사용 | `register_account()` |
| @DisplayName | "~~~한다" 형태로 기대 동작 작성 | `@DisplayName("계좌를 등록한다")` |

### 6.5 테스트 파일 구조

```
account/src/test/java/com/leesuchan/account/
├── domain/
│   └── model/
│       └── AccountTest.java                    # 도메인 테스트
├── service/
│   ├── RegisterAccountUseCaseTest.java         # UseCase 단위 테스트
│   ├── DeleteAccountUseCaseTest.java
│   ├── DepositMoneyUseCaseTest.java
│   ├── WithdrawMoneyUseCaseTest.java
│   └── TransferMoneyUseCaseTest.java
service/src/test/java/com/leesuchan/service/
└── AccountControllerE2ETest.java               # Controller E2E 테스트
```
