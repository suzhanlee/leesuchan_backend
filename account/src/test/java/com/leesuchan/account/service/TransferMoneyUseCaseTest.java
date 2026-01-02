package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.exception.DailyTransferLimitExceededException;
import com.leesuchan.account.domain.exception.InsufficientBalanceException;
import com.leesuchan.account.domain.exception.SameAccountTransferException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.account.service.dto.TransferRequest;
import com.leesuchan.activity.service.ActivityRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferMoneyUseCase 테스트")
class TransferMoneyUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActivityRecordService activityRecordService;

    private TransferMoneyUseCase transferMoneyUseCase;

    @BeforeEach
    void setUp() {
        transferMoneyUseCase = new TransferMoneyUseCase(accountRepository, activityRecordService);
    }

    /**
     * 테스트용 Account ID 설정
     */
    private void setAccountId(Account account, Long id) {
        try {
            Field field = Account.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(account, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("계좌 간 이체한다")
    void transfer_money() {
        // given
        String fromAccountNumber = "1234567890";
        String toAccountNumber = "0987654321";
        Long amount = 10000L;

        Account from = Account.create(fromAccountNumber, "출금 계좌");
        from.deposit(50000L); // 잔액 50000원
        setAccountId(from, 1L);
        Account to = Account.create(toAccountNumber, "입금 계좌");
        setAccountId(to, 2L);

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(java.util.Optional.of(from));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(java.util.Optional.of(to));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordTransferOut(any(), any(), any(), any(), any(), any(), any());
        doNothing().when(activityRecordService).recordTransferIn(any(), any(), any(), any(), any(), any());

        // when
        TransferMoneyUseCase.TransferResult result = transferMoneyUseCase.execute(fromAccountNumber, toAccountNumber, amount);

        // then
        assertThat(result.from().getBalance()).isEqualTo(39900L); // 50000 - 10000 - 100(수수료)
        assertThat(result.to().getBalance()).isEqualTo(10000L);
        assertThat(result.fee()).isEqualTo(100L); // 1% 수수료

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(activityRecordService).recordTransferOut(any(), eq(to.getId()), eq(toAccountNumber), eq(amount), eq(100L), eq(39900L), any());
        verify(activityRecordService).recordTransferIn(eq(to.getId()), eq(from.getId()), eq(fromAccountNumber), eq(amount), eq(10000L), any());
    }

    @Test
    @DisplayName("출금 계좌가 없으면 예외가 발생한다")
    void transfer_from_account_not_found_throws_exception() {
        // given
        String fromAccountNumber = "1234567890";
        String toAccountNumber = "0987654321";
        Long amount = 10000L;

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> transferMoneyUseCase.execute(fromAccountNumber, toAccountNumber, amount))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository, never()).save(any(Account.class));
        verify(activityRecordService, never()).recordTransferOut(any(), any(), any(), any(), any(), any(), any());
        verify(activityRecordService, never()).recordTransferIn(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("입금 계좌가 없으면 예외가 발생한다")
    void transfer_to_account_not_found_throws_exception() {
        // given
        String fromAccountNumber = "1234567890";
        String toAccountNumber = "0987654321";
        Long amount = 10000L;

        Account from = Account.create(fromAccountNumber, "출금 계좌");
        from.deposit(50000L);
        setAccountId(from, 1L);

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(java.util.Optional.of(from));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> transferMoneyUseCase.execute(fromAccountNumber, toAccountNumber, amount))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("동일 계좌로 이체하면 예외가 발생한다")
    void transfer_to_same_account_throws_exception() {
        // given
        String accountNumber = "1234567890";
        Long amount = 10000L;

        Account account = Account.create(accountNumber, "계좌");
        account.deposit(50000L);
        setAccountId(account, 1L);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));

        // when & then
        assertThatThrownBy(() -> transferMoneyUseCase.execute(accountNumber, accountNumber, amount))
                .isInstanceOf(SameAccountTransferException.class);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("일일 한도를 초과하면 예외가 발생한다")
    void transfer_exceeds_daily_limit_throws_exception() {
        // given
        String fromAccountNumber = "1234567890";
        String toAccountNumber = "0987654321";
        Long amount = 3_000_001L;

        Account from = Account.create(fromAccountNumber, "출금 계좌");
        from.deposit(5_000_000L);
        setAccountId(from, 1L);
        Account to = Account.create(toAccountNumber, "입금 계좌");
        setAccountId(to, 2L);

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(java.util.Optional.of(from));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(java.util.Optional.of(to));

        // when & then
        assertThatThrownBy(() -> transferMoneyUseCase.execute(fromAccountNumber, toAccountNumber, amount))
                .isInstanceOf(DailyTransferLimitExceededException.class);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("잔액이 부족하면 예외가 발생한다")
    void transfer_insufficient_balance_throws_exception() {
        // given
        String fromAccountNumber = "1234567890";
        String toAccountNumber = "0987654321";
        Long amount = 10000L;

        Account from = Account.create(fromAccountNumber, "출금 계좌");
        from.deposit(5000L); // 잔액 5000원 (수수료 포함 부족)
        setAccountId(from, 1L);
        Account to = Account.create(toAccountNumber, "입금 계좌");
        setAccountId(to, 2L);

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(java.util.Optional.of(from));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(java.util.Optional.of(to));

        // when & then
        assertThatThrownBy(() -> transferMoneyUseCase.execute(fromAccountNumber, toAccountNumber, amount))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("DTO로 이체한다")
    void transfer_money_with_dto() {
        // given
        String fromAccountNumber = "1234567890";
        String toAccountNumber = "0987654321";
        Long amount = 5000L;

        TransferRequest request = new TransferRequest(fromAccountNumber, toAccountNumber, amount);

        Account from = Account.create(fromAccountNumber, "출금 계좌");
        from.deposit(20000L);
        setAccountId(from, 1L);
        Account to = Account.create(toAccountNumber, "입금 계좌");
        setAccountId(to, 2L);

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(java.util.Optional.of(from));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(java.util.Optional.of(to));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordTransferOut(any(), any(), any(), any(), any(), any(), any());
        doNothing().when(activityRecordService).recordTransferIn(any(), any(), any(), any(), any(), any());

        // when
        TransferMoneyUseCase.TransferResult result = transferMoneyUseCase.execute(request);

        // then
        assertThat(result.from().getBalance()).isEqualTo(14950L); // 20000 - 5000 - 50(수수료)
        assertThat(result.to().getBalance()).isEqualTo(5000L);
        assertThat(result.fee()).isEqualTo(50L);
    }
}
