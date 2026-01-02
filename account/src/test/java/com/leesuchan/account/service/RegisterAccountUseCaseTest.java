package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.DuplicateAccountException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.account.service.dto.RegisterAccountRequest;
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
        String accountName = "테스트 계좌";
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Account account = registerAccountUseCase.execute(accountNumber, accountName);

        // then
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getAccountName()).isEqualTo(accountName);
        assertThat(account.getBalance()).isZero();
        verify(accountRepository).existsByAccountNumber(eq(accountNumber));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("중복 계좌번호로 등록하면 예외가 발생한다")
    void register_duplicate_account_throws_exception() {
        // given
        String accountNumber = "1234567890";
        String accountName = "테스트 계좌";
        when(accountRepository.existsByAccountNumber(any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> registerAccountUseCase.execute(accountNumber, accountName))
                .isInstanceOf(DuplicateAccountException.class);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("DTO로 계좌를 등록한다")
    void register_account_with_dto() {
        // given
        String accountNumber = "1234567890";
        String accountName = "테스트 계좌";
        RegisterAccountRequest request = new RegisterAccountRequest(accountNumber, accountName);
        when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
        when(accountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Account account = registerAccountUseCase.execute(request);

        // then
        assertThat(account.getAccountNumber()).isEqualTo(accountNumber);
        assertThat(account.getAccountName()).isEqualTo(accountName);
        assertThat(account.getBalance()).isZero();
        verify(accountRepository).save(any(Account.class));
    }
}
