package com.leesuchan.account.domain.model;

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
}
