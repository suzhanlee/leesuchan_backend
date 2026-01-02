package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.account.service.dto.DepositRequest;
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
@DisplayName("DepositMoneyUseCase 테스트")
class DepositMoneyUseCaseTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ActivityRecordService activityRecordService;

    private DepositMoneyUseCase depositMoneyUseCase;

    @BeforeEach
    void setUp() {
        depositMoneyUseCase = new DepositMoneyUseCase(accountRepository, activityRecordService);
    }

    @Test
    @DisplayName("계좌에 입금한다")
    void deposit_money() {
        // given
        String accountNumber = "1234567890";
        Long amount = 10000L;
        Account account = Account.create(accountNumber, "테스트 계좌");

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordDeposit(any(), any(), any());

        // when
        Account result = depositMoneyUseCase.execute(accountNumber, amount);

        // then
        assertThat(result.getBalance()).isEqualTo(10000L);
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(account);
        verify(activityRecordService).recordDeposit(account.getId(), amount, 10000L);
    }

    @Test
    @DisplayName("존재하지 않는 계좌에 입금하면 예외가 발생한다")
    void deposit_not_exist_account_throws_exception() {
        // given
        String accountNumber = "1234567890";
        Long amount = 10000L;

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> depositMoneyUseCase.execute(accountNumber, amount))
                .isInstanceOf(AccountNotFoundException.class);
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any());
        verify(activityRecordService, never()).recordDeposit(any(), any(), any());
    }

    @Test
    @DisplayName("DTO로 입금한다")
    void deposit_money_with_dto() {
        // given
        String accountNumber = "1234567890";
        Long amount = 5000L;
        DepositRequest request = new DepositRequest(accountNumber, amount);
        Account account = Account.create(accountNumber, "테스트 계좌");

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordDeposit(any(), any(), any());

        // when
        Account result = depositMoneyUseCase.execute(request);

        // then
        assertThat(result.getBalance()).isEqualTo(5000L);
        verify(accountRepository).save(account);
        verify(activityRecordService).recordDeposit(account.getId(), amount, 5000L);
    }

    @Test
    @DisplayName("여러 번 입금하면 잔액이 누적된다")
    void deposit_multiple_times_accumulates_balance() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "테스트 계좌");

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(activityRecordService).recordDeposit(any(), any(), any());

        // when
        depositMoneyUseCase.execute(accountNumber, 10000L);
        depositMoneyUseCase.execute(accountNumber, 5000L);

        // then
        assertThat(account.getBalance()).isEqualTo(15000L);
        verify(activityRecordService, times(2)).recordDeposit(any(), any(), any());
    }
}
