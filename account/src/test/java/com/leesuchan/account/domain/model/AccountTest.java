package com.leesuchan.account.domain.model;

import com.leesuchan.account.domain.exception.DailyTransferLimitExceededException;
import com.leesuchan.account.domain.exception.DailyWithdrawLimitExceededException;
import com.leesuchan.account.domain.exception.InvalidAccountNameException;
import com.leesuchan.account.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account 엔티티 테스트")
class AccountTest {

    private String accountNumber;
    private String accountName;

    @BeforeEach
    void setUp() {
        accountNumber = "1234567890";
        accountName = "테스트 계좌";
    }

    @Test
    @DisplayName("계좌를 생성한다")
    void create_account() {
        // when
        Account account = Account.create(accountNumber, accountName);

        // then
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getAccountName()).isEqualTo(accountName);
        assertThat(account.getBalance()).isZero();
        assertThat(account.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("빈 계좌명으로 생성하면 예외가 발생한다")
    void create_account_with_empty_name_throws_exception() {
        // when & then
        assertThatThrownBy(() -> Account.create(accountNumber, ""))
                .isInstanceOf(InvalidAccountNameException.class);
    }

    @Test
    @DisplayName("긴 계좌명으로 생성하면 예외가 발생한다")
    void create_account_with_long_name_throws_exception() {
        // given
        String longName = "A".repeat(101);

        // when & then
        assertThatThrownBy(() -> Account.create(accountNumber, longName))
                .isInstanceOf(InvalidAccountNameException.class);
    }

    @Test
    @DisplayName("유효하지 않은 계좌번호로 생성하면 예외가 발생한다")
    void create_account_with_invalid_number_throws_exception() {
        // when & then
        assertThatThrownBy(() -> Account.create("ab", accountName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("계좌번호는 3~20자여야 합니다.");
    }

    @Test
    @DisplayName("입금한다")
    void deposit() {
        // given
        Account account = Account.create(accountNumber, accountName);
        long amount = 5000L;

        // when
        account.deposit(amount);

        // then
        assertThat(account.getBalance()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("0원 이하로 입금하면 예외가 발생한다")
    void deposit_zero_or_negative_throws_exception() {
        // given
        Account account = Account.create(accountNumber, accountName);

        // when & then
        assertThatThrownBy(() -> account.deposit(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("출금한다")
    void withdraw() {
        // given
        Account account = Account.create(accountNumber, accountName);
        account.deposit(10000L);

        // when
        account.withdraw(3000L);

        // then
        assertThat(account.getBalance()).isEqualTo(7000L);
    }

    @Test
    @DisplayName("잔액보다 많이 출금하면 예외가 발생한다")
    void withdraw_more_than_balance_throws_exception() {
        // given
        Account account = Account.create(accountNumber, accountName);
        account.deposit(5000L);

        // when & then
        assertThatThrownBy(() -> account.withdraw(10000L))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("소프트 삭제한다")
    void delete_account() {
        // given
        Account account = Account.create(accountNumber, accountName);

        // when
        account.delete();

        // then
        assertThat(account.isDeleted()).isTrue();
        assertThat(account.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("일일 출금 한도 내에서 여러 번 출금할 수 있다")
    void withdraw_multiple_times_within_daily_limit() {
        // given
        Account account = Account.create(accountNumber, accountName);
        account.deposit(2_000_000L);

        // when
        account.withdraw(500_000L);
        account.withdraw(400_000L);

        // then
        assertThat(account.getBalance()).isEqualTo(1_100_000L);
    }

    @Test
    @DisplayName("일일 출금 한도를 초과하면 예외가 발생한다")
    void withdraw_exceeds_daily_limit_throws_exception() {
        // given
        Account account = Account.create(accountNumber, accountName);
        account.deposit(2_000_000L);
        account.withdraw(500_000L);

        // when & then
        assertThatThrownBy(() -> account.withdraw(500_001L))
                .isInstanceOf(DailyWithdrawLimitExceededException.class);
    }

    @Test
    @DisplayName("일일 이체 한도 내에서 여러 번 이체할 수 있다")
    void transfer_multiple_times_within_daily_limit() {
        // given
        Account from = Account.create(accountNumber, "출금 계좌");
        Account to = Account.create("0987654321", "입금 계좌");
        from.deposit(5_000_000L);

        // when
        from.transfer(to, 1_000_000L);  // 수수료: 10,000원
        from.transfer(to, 1_500_000L);  // 수수료: 15,000원

        // then
        // 5,000,000 - 1,000,000 - 10,000 - 1,500,000 - 15,000 = 2,475,000
        assertThat(from.getBalance()).isEqualTo(2_475_000L);
        assertThat(to.getBalance()).isEqualTo(2_500_000L);
    }

    @Test
    @DisplayName("일일 이체 한도를 초과하면 예외가 발생한다")
    void transfer_exceeds_daily_limit_throws_exception() {
        // given
        Account from = Account.create(accountNumber, "출금 계좌");
        Account to = Account.create("0987654321", "입금 계좌");
        from.deposit(5_000_000L);
        from.transfer(to, 2_000_000L);

        // when & then
        assertThatThrownBy(() -> from.transfer(to, 1_000_001L))
                .isInstanceOf(DailyTransferLimitExceededException.class);
    }

    @Test
    @DisplayName("이체 수수료가 1%로 계산된다")
    void transfer_fee_is_1_percent() {
        // given
        Account from = Account.create(accountNumber, "출금 계좌");
        Account to = Account.create("0987654321", "입금 계좌");
        from.deposit(1_000_000L);

        // when
        from.transfer(to, 100_000L);

        // then
        assertThat(from.getBalance()).isEqualTo(899_000L); // 1,000,000 - 100,000 - 1,000(수수료)
        assertThat(to.getBalance()).isEqualTo(100_000L);
    }
}
