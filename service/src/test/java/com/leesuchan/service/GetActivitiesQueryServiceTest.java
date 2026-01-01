package com.leesuchan.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.model.ActivityType;
import com.leesuchan.activity.domain.repository.ActivityRepository;
import com.leesuchan.service.application.GetActivitiesQueryService;
import com.leesuchan.service.dto.response.ActivityResponse;
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
@DisplayName("GetActivitiesQueryService 테스트")
class GetActivitiesQueryServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private AccountRepository accountRepository;

    private GetActivitiesQueryService getActivitiesQueryService;

    @BeforeEach
    void setUp() {
        getActivitiesQueryService = new GetActivitiesQueryService(activityRepository, accountRepository);
    }

    @Test
    @DisplayName("계좌의 거래내역을 조회한다")
    void get_activities() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "테스트 계좌");

        Activity activity1 = Activity.deposit(1L, 10000L, 10000L);
        Activity activity2 = Activity.withdraw(1L, 5000L, 5000L);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(activityRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()))
                .thenReturn(java.util.List.of(activity1, activity2));

        // when
        java.util.List<ActivityResponse> responses = getActivitiesQueryService.execute(accountNumber);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).activityType()).isEqualTo(ActivityType.DEPOSIT);
        assertThat(responses.get(0).amount()).isEqualTo(10000L);
        assertThat(responses.get(1).activityType()).isEqualTo(ActivityType.WITHDRAW);
        assertThat(responses.get(1).amount()).isEqualTo(5000L);

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(activityRepository).findByAccountIdOrderByCreatedAtDesc(account.getId());
    }

    @Test
    @DisplayName("계좌가 없으면 예외가 발생한다")
    void account_not_found_throws_exception() {
        // given
        String accountNumber = "1234567890";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> getActivitiesQueryService.execute(accountNumber))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(activityRepository, never()).findByAccountIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("거래내역이 없으면 빈 목록을 반환한다")
    void no_activities_returns_empty_list() {
        // given
        String accountNumber = "1234567890";
        Account account = Account.create(accountNumber, "테스트 계좌");

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(java.util.Optional.of(account));
        when(activityRepository.findByAccountIdOrderByCreatedAtDesc(account.getId()))
                .thenReturn(java.util.List.of());

        // when
        java.util.List<ActivityResponse> responses = getActivitiesQueryService.execute(accountNumber);

        // then
        assertThat(responses).isEmpty();

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(activityRepository).findByAccountIdOrderByCreatedAtDesc(account.getId());
    }
}
