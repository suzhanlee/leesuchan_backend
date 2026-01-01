package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
