package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.exception.DailyWithdrawLimitExceededException;
import com.leesuchan.account.domain.exception.InsufficientBalanceException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.activity.service.ActivityRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WithdrawMoneyUseCase 테스트")
class WithdrawMoneyUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActivityRecordService activityRecordService;

    private WithdrawMoneyUseCase withdrawMoneyUseCase;

    @BeforeEach
    void setUp() {
        withdrawMoneyUseCase = new WithdrawMoneyUseCase(accountRepository, activityRecordService);
    }

    @Test
    @DisplayName("계좌에서 출금한다")
    void withdraw_money() {
        // given
        String accountNumber = "1234567890";
        Long amount = 5000L;
        Account account = Account.create(accountNumber, "테스트 계좌");
        account.deposit(10000L); // 잔액 10000원

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordWithdraw(any(), any(), any());

        // when
        Account result = withdrawMoneyUseCase.execute(accountNumber, amount);

        // then
        assertThat(result.getBalance()).isEqualTo(5000L);
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(account);
        verify(activityRecordService).recordWithdraw(account.getId(), amount, 5000L);
    }

    @Test
    @DisplayName("존재하지 않는 계좌에서 출금하면 예외가 발생한다")
    void withdraw_not_exist_account_throws_exception() {
        // given
        String accountNumber = "1234567890";
        Long amount = 5000L;

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> withdrawMoneyUseCase.execute(accountNumber, amount))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any());
        verify(activityRecordService, never()).recordWithdraw(any(), any(), any());
    }

    @Test
    @DisplayName("일일 한도를 초과하면 예외가 발생한다")
    void withdraw_exceeds_daily_limit_throws_exception() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "테스트 계좌");
        account.deposit(2_000_000L); // 잔액 200만원

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        withdrawMoneyUseCase.execute(accountNumber, 1_000_000L); // 100만원 출금

        // then - 한도 초과 예외 발생
        assertThatThrownBy(() -> withdrawMoneyUseCase.execute(accountNumber, 1L))
                .isInstanceOf(DailyWithdrawLimitExceededException.class);
    }

    @Test
    @DisplayName("잔액이 부족하면 예외가 발생한다")
    void withdraw_insufficient_balance_throws_exception() {
        // given
        String accountNumber = "1234567890";
        Long amount = 10000L;
        Account account = Account.create(accountNumber, "테스트 계좌");
        account.deposit(5000L); // 잔액 5000원

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));

        // when & then
        assertThatThrownBy(() -> withdrawMoneyUseCase.execute(accountNumber, amount))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any());
        verify(activityRecordService, never()).recordWithdraw(any(), any(), any());
    }

    @Test
    @DisplayName("DTO로 출금한다")
    void withdraw_money_with_dto() {
        // given
        String accountNumber = "1234567890";
        Long amount = 3000L;
        WithdrawRequest request = new WithdrawRequest(accountNumber, amount);
        Account account = Account.create(accountNumber, "테스트 계좌");
        account.deposit(10000L);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordWithdraw(any(), any(), any());

        // when
        Account result = withdrawMoneyUseCase.execute(request);

        // then
        assertThat(result.getBalance()).isEqualTo(7000L);
        verify(accountRepository).save(account);
        verify(activityRecordService).recordWithdraw(account.getId(), amount, 7000L);
    }

    @Test
    @DisplayName("날짜가 바뀌면 일일 한도가 리셋된다")
    void daily_limit_resets_on_new_day() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "테스트 계좌");
        account.deposit(2_000_000L);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordWithdraw(any(), any(), any());

        // when - 첫날 100만원 출금
        withdrawMoneyUseCase.execute(accountNumber, 1_000_000L);

        // then - 누적 출금액 확인
        assertThat(account.getWithdrawLimitTracker().getAccumulatedAmount()).isEqualTo(1_000_000L);
    }
}
