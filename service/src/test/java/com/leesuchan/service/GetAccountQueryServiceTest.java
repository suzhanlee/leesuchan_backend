package com.leesuchan.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.service.application.GetAccountQueryService;
import com.leesuchan.service.dto.response.AccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAccountQueryService 테스트")
class GetAccountQueryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private GetAccountQueryService getAccountQueryService;

    @BeforeEach
    void setUp() {
        getAccountQueryService = new GetAccountQueryService(accountRepository);
    }

    @Test
    @DisplayName("계좌를 조회한다")
    void get_account() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "테스트 계좌");
        account.deposit(10000L);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // when
        AccountResponse response = getAccountQueryService.execute(accountNumber);

        // then
        assertThat(response.accountNumber()).isEqualTo(accountNumber);
        assertThat(response.accountName()).isEqualTo("테스트 계좌");
        assertThat(response.balance()).isEqualTo(10000L);

        verify(accountRepository).findByAccountNumber(accountNumber);
    }

    @Test
    @DisplayName("계좌가 없으면 예외가 발생한다")
    void account_not_found_throws_exception() {
        // given
        String accountNumber = "1234567890";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getAccountQueryService.execute(accountNumber))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository).findByAccountNumber(accountNumber);
    }
}
