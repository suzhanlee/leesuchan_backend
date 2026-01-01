# 송금 서비스 아키텍처

## 개요
계좌 간 송금 시스템을 DDD + CQRS + 멀티모듈 아키텍처로 구현합니다.

## 아키텍처

### 멀티모듈 구조
```
leesuchan_backend/
├── account/          # Account Aggregate
├── activity/         # Activity Aggregate (거래내역)
├── common/           # 공통 모듈 (ApiResponse, Error)
├── service/          # 웹 계층 (Controller)
└── infra/            # 인프라 계층
    ├── database/     # JPA 영속성
    ├── external/     # 외부 API 연동
    └── flyway/       # DB 마이그레이션
```

### DDD 설계
- **Account Aggregate**: 계좌, 잔액, 일일 한도 관리
- **Activity Aggregate**: 거래 내역 (입금/출금/이체)
- **Aggregate Root**: JPA Entity로 직접 구현 (Spring AbstractAggregateRoot 미사용)

### CQRS
- **Command**: account/service (등록, 삭제, 입금, 출금, 이체)
- **Query**: activity/service (거래 내역 조회)

### 동시성
- JPA 낙관적 락 (`@Version`)
- 재시도 메커니즘

## 한도 규칙
- 출금 한도: 일일 1,000,000원
- 이체 한도: 일일 3,000,000원
- 이체 수수료: 이체 금액의 1%

## 실행 방법
```bash
docker-compose up -d
./gradlew :service:bootRun
```

---

## 코딩 컨벤션 (Coding Conventions)

### 1. 빈 등록 (Bean Registration)

#### UseCase (서비스)
```java
@Service
public class RegisterAccountUseCase {
    private final AccountRepository accountRepository;

    // 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
    public RegisterAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
}
```
- `@Service` 어노테이션 사용
- `@RequiredArgsConstructor` 대신 명시적 생성자 주입
- 생성자가 하나인 경우 `@Autowired` 생략 가능

#### Repository 구현체
```java
@Repository
public class AccountRepositoryImpl implements AccountRepository {
    private final AccountJpaRepository jpaRepository;

    // 생성자 주입
    public AccountRepositoryImpl(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
}
```
- `@Repository` 어노테이션 사용
- Port 인터페이스 구현
- 생성자 주입 방식

#### Controller
```java
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final RegisterAccountUseCase registerAccountUseCase;

    // 생성자 주입
    public AccountController(RegisterAccountUseCase registerAccountUseCase) {
        this.registerAccountUseCase = registerAccountUseCase;
    }
}
```
- `@RestController` 사용
- 생성자 주입 방식

### 2. 도메인 예외 (Domain Exceptions)

#### 도메인 예외 정의
```java
public class InvalidAccountNameException extends DomainException {
    public InvalidAccountNameException(String message) {
        super(AccountErrorCode.INVALID_NAME);
    }
}
```
- 모든 도메인 예외는 `DomainException` 상속
- `ErrorCode`는 상수 인터페이스로 관리 (`AccountErrorCode`)

#### 에러 코드 정의
```java
public interface AccountErrorCode extends ErrorCode {
    ErrorCode NOT_FOUND = of("ACCOUNT_001", "계좌를 찾을 수 없습니다.");
    ErrorCode DUPLICATE = of("ACCOUNT_002", "이미 존재하는 계좌번호입니다.");
    // ...
}
```

#### 엔티티에서 도메인 예외 사용
```java
@Entity
public class Account {
    Account(String accountNumber, String accountName, Long balance) {
        if (accountName == null || accountName.isBlank()) {
            throw new InvalidAccountNameException("계좌명은 비어있을 수 없습니다.");
        }
        // ...
    }
}
```
- 도메인 로직 검증 실패 시 `DomainException` 상속 예외 사용
- `IllegalArgumentException`은 간단한 값 검증에만 제한적 사용

### 3. Value Object (YAGNI 원칙)

#### VO 사용 기준
- **과하게 사용하지 않음** (YAGNI: You Aren't Gonna Need It)
- 정말 필요한 경우에만 VO 사용: 검증 로직이 복잡하거나 도메인에 특별한 의미가 있는 경우
- 계좌번호, 잔액 등은 `String`, `Long` 등 기본 타입으로 충분

#### 일반적인 경우 (String 사용)
```java
@Entity
public class Account {
    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    private Long balance;

    private static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 비어있을 수 없습니다.");
        }
        if (accountNumber.length() < 3 || accountNumber.length() > 20) {
            throw new IllegalArgumentException("계좌번호는 3~20자여야 합니다.");
        }
    }
}
```

### 4. record vs class

#### record 사용 (DTO)
```java
// API Request/Response DTO
public record RegisterAccountDto(
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

#### class 사용 (Entity, 팩토리)
```java
// JPA Entity
@Entity
public class Account {
    // JPA 제약으로 class 사용
}

// 팩토리 메서드 패턴이 필요한 경우
public class Status {
    private Status(boolean success, String code, String message) { ... }
    public static Status success() { ... }
    public static Status error(String code) { ... }
}
```
- **JPA Entity**: `class` 사용 (JPA 제약)
- **팩토리 메서드 패턴**: `class` 사용
- **내부 상태 캡슐화**가 필요한 경우

### 5. Setter 금지

#### 엔티티
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
- 모든 필드는 불변 또는 패키지 private
- 상태 변경은 도메인 메서드로만 (`deposit()`, `withdraw()` 등)
- JPA를 위한 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 허용

### 6. 테스트 작성 (Test)

#### UseCase 단위 테스트
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteAccountUseCase 테스트")
class DeleteAccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    private DeleteAccountUseCase deleteAccountUseCase;

    @BeforeEach
    void setUp() {
        deleteAccountUseCase = new DeleteAccountUseCase(accountRepository);
    }

    @Test
    @DisplayName("계좌를 삭제한다")
    void delete_account() {
        // given
        String accountNumber = "1234567890";
        doNothing().when(accountRepository).deleteByAccountNumber(any());

        // when
        deleteAccountUseCase.delete(accountNumber);

        // then
        verify(accountRepository).deleteByAccountNumber(eq(accountNumber));
    }

    @Test
    @DisplayName("존재하지 않는 계좌를 삭제하면 예외가 발생한다")
    void delete_not_exist_account_throws_exception() {
        // given
        String accountNumber = "1234567890";
        doThrow(AccountNotFoundException.class).when(accountRepository).deleteByAccountNumber(any());

        // when & then
        assertThatThrownBy(() -> deleteAccountUseCase.delete(accountNumber))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository).deleteByAccountNumber(eq(accountNumber));
    }
}
```
- `@ExtendWith(MockitoExtension.class)` 사용
- `@Mock`으로 의존성 주입
- given-when-then 패턴 사용
- `doNothing()`, `doThrow()`로 Mock 설정
- AssertJ `assertThatThrownBy()`로 예외 검증

#### Controller E2E 테스트
```java
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("계좌 삭제 E2E 테스트")
class DeleteAccountE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeleteAccountUseCase deleteAccountUseCase;

    @MockBean
    private RegisterAccountUseCase registerAccountUseCase;

    @BeforeEach
    void setUp() {
        reset(deleteAccountUseCase);
    }

    @Test
    @DisplayName("계좌를 삭제하는 API를 호출한다")
    void delete_account_api() throws Exception {
        // given
        String accountNumber = "1234567890";
        doNothing().when(deleteAccountUseCase).delete(any());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.status.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(deleteAccountUseCase).delete(eq(accountNumber));
    }
}
```
- `@SpringBootTest` + `@AutoConfigureMockMvc` 사용
  - `@WebMvcTest`는 JPA EntityManagerFactory 로드하지 않아 @EnableJpaRepositories와 충돌
- `@MockBean`으로 UseCase Mock 주입
- `MockMvc`로 HTTP 요청/응답 테스트
- `jsonPath()`로 JSON 응답 검증

#### 테스트 명명 규칙
- 클래스명: `XxxTest` (단위 테스트), `XxxE2ETest` (E2E 테스트)
- 메서드명: snake_case, 한국어 @DisplayName 사용
- `@DisplayName`에 "~~~한다" 형태로 기대 동작 작성

---

## 커밋 규칙 (Commit Convention)

### 커밋 메시지 형식
```
<타입>: <제목>

<본문 (선택)>
```

### 타입 (Type)
| 타입 | 설명 | 예시 |
|------|------|------|
| feat | 새로운 기능 추가 | feat: 계좌 등록 API 구현 |
| fix | 버그 수정 | fix: 출금 한도 체크 로직 수정 |
| docs | 문서 수정 | docs: README 실행 방법 추가 |
| style | 코드 스타일 (포맷팅) | style: import 정리 |
| refactor | 리팩토링 | refactor: Account 잔액 로직 분리 |
| test | 테스트 코드 | test: AccountService 단위 테스트 추가 |
| chore | 빌드/설정 | chore: Lombok 의존성 추가 |
| config | 설정 파일 | config: JPA Auditing 설정 추가 |

### 예시
```
feat: Account Aggregate 도메인 구현

- Account 엔티티 추가 (AbstractAggregateRoot 상속)
- Money Value Object 추가
- AccountRepository Port 인터페이스 추가
- 소프트 삭제 지원 (deletedAt)
```

### 주의사항
- 제목은 한국어로 작성
- 제목은 50자 이내
- 제목 끝에 마침표(.) 사용하지 않음
- 본문은 어떻게보다 무엇을/왜 변경했는지 작성
- 한 기능 완성 시마다 커밋 (한꺼번에 몰아서 커밋 금지)
