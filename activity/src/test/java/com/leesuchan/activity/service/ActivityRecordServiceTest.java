package com.leesuchan.activity.service;

import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.model.ActivityType;
import com.leesuchan.activity.domain.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityRecordService 테스트")
class ActivityRecordServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    private ActivityRecordService activityRecordService;

    @BeforeEach
    void setUp() {
        activityRecordService = new ActivityRecordService(activityRepository);
    }

    @Test
    @DisplayName("입금 거래내역을 기록한다")
    void record_deposit() {
        // given
        Long accountId = 1L;
        Long amount = 10000L;
        Long balanceAfter = 10000L;
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        activityRecordService.recordDeposit(accountId, amount, balanceAfter);

        // then
        verify(activityRepository).save(argThat(activity ->
                activity.getAccountId().equals(accountId) &&
                        activity.getActivityType() == ActivityType.DEPOSIT &&
                        activity.getAmount().equals(amount) &&
                        activity.getBalanceAfter().equals(balanceAfter) &&
                        activity.getFee() == 0L
        ));
    }

    @Test
    @DisplayName("출금 거래내역을 기록한다")
    void record_withdraw() {
        // given
        Long accountId = 1L;
        Long amount = 5000L;
        Long balanceAfter = 5000L;
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        activityRecordService.recordWithdraw(accountId, amount, balanceAfter);

        // then
        verify(activityRepository).save(argThat(activity ->
                activity.getAccountId().equals(accountId) &&
                        activity.getActivityType() == ActivityType.WITHDRAW &&
                        activity.getAmount().equals(amount) &&
                        activity.getBalanceAfter().equals(balanceAfter) &&
                        activity.getFee() == 0L
        ));
    }

    @Test
    @DisplayName("이체 출금 거래내역을 기록한다")
    void record_transfer_out() {
        // given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        String toAccountNumber = "0987654321";
        Long amount = 10000L;
        Long fee = 100L;
        Long balanceAfter = 39900L;
        String transactionId = "tx-12345";
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        activityRecordService.recordTransferOut(
                fromAccountId,
                toAccountId,
                toAccountNumber,
                amount,
                fee,
                balanceAfter,
                transactionId
        );

        // then
        verify(activityRepository).save(argThat(activity ->
                activity.getAccountId().equals(fromAccountId) &&
                        activity.getActivityType() == ActivityType.TRANSFER_OUT &&
                        activity.getAmount().equals(amount) &&
                        activity.getFee().equals(fee) &&
                        activity.getBalanceAfter().equals(balanceAfter) &&
                        activity.getReferenceAccountId().equals(toAccountId) &&
                        activity.getReferenceAccountNumber().equals(toAccountNumber) &&
                        activity.getTransactionId().equals(transactionId)
        ));
    }

    @Test
    @DisplayName("이체 입금 거래내역을 기록한다")
    void record_transfer_in() {
        // given
        Long toAccountId = 2L;
        Long fromAccountId = 1L;
        String fromAccountNumber = "1234567890";
        Long amount = 10000L;
        Long balanceAfter = 20000L;
        String transactionId = "tx-12345";
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        activityRecordService.recordTransferIn(
                toAccountId,
                fromAccountId,
                fromAccountNumber,
                amount,
                balanceAfter,
                transactionId
        );

        // then
        verify(activityRepository).save(argThat(activity ->
                activity.getAccountId().equals(toAccountId) &&
                        activity.getActivityType() == ActivityType.TRANSFER_IN &&
                        activity.getAmount().equals(amount) &&
                        activity.getFee() == 0L && // 입금자는 수수료 없음
                        activity.getBalanceAfter().equals(balanceAfter) &&
                        activity.getReferenceAccountId().equals(fromAccountId) &&
                        activity.getReferenceAccountNumber().equals(fromAccountNumber) &&
                        activity.getTransactionId().equals(transactionId)
        ));
    }

    @Test
    @DisplayName("이체 거래내역을 기록한다 (출금/입금 쌍)")
    void record_transfer() {
        // given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        String toAccountNumber = "0987654321";
        Long amount = 10000L;
        Long fee = 100L;
        Long fromBalanceAfter = 39900L;
        Long toBalanceAfter = 20000L;
        String transactionId = "tx-12345";
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        activityRecordService.recordTransfer(
                fromAccountId,
                toAccountId,
                toAccountNumber,
                amount,
                fee,
                fromBalanceAfter,
                toBalanceAfter,
                transactionId
        );

        // then
        verify(activityRepository, times(2)).save(any(Activity.class));

        verify(activityRepository).save(argThat(activity ->
                activity.getAccountId().equals(fromAccountId) &&
                        activity.getActivityType() == ActivityType.TRANSFER_OUT &&
                        activity.getFee().equals(fee)
        ));

        verify(activityRepository).save(argThat(activity ->
                activity.getAccountId().equals(toAccountId) &&
                        activity.getActivityType() == ActivityType.TRANSFER_IN &&
                        activity.getFee() == 0L && // 입금자는 수수료 없음
                        activity.getReferenceAccountNumber() == null // 상대방 계좌번호는 입금자에게 표시하지 않음
        ));
    }
}
