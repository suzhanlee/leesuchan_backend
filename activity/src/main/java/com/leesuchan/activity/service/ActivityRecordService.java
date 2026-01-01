package com.leesuchan.activity.service;

import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.repository.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 거래내역 기록 서비스
 */
@Service
public class ActivityRecordService {

    private final ActivityRepository activityRepository;

    public ActivityRecordService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    /**
     * 입금 거래내역을 기록합니다.
     */
    @Transactional
    public void recordDeposit(Long accountId, Long amount, Long balanceAfter) {
        Activity activity = Activity.deposit(accountId, amount, balanceAfter);
        activityRepository.save(activity);
    }

    /**
     * 출금 거래내역을 기록합니다.
     */
    @Transactional
    public void recordWithdraw(Long accountId, Long amount, Long balanceAfter) {
        Activity activity = Activity.withdraw(accountId, amount, balanceAfter);
        activityRepository.save(activity);
    }

    /**
     * 이체 출금 거래내역을 기록합니다.
     */
    @Transactional
    public void recordTransferOut(
            Long fromAccountId,
            Long toAccountId,
            String toAccountNumber,
            Long amount,
            Long fee,
            Long balanceAfter,
            String transactionId
    ) {
        Activity activity = Activity.transferOut(
                fromAccountId,
                toAccountId,
                toAccountNumber,
                amount,
                fee,
                balanceAfter,
                transactionId
        );
        activityRepository.save(activity);
    }

    /**
     * 이체 입금 거래내역을 기록합니다.
     */
    @Transactional
    public void recordTransferIn(
            Long toAccountId,
            Long fromAccountId,
            String fromAccountNumber,
            Long amount,
            Long balanceAfter,
            String transactionId
    ) {
        Activity activity = Activity.transferIn(
                toAccountId,
                fromAccountId,
                fromAccountNumber,
                amount,
                balanceAfter,
                transactionId
        );
        activityRepository.save(activity);
    }

    /**
     * 이체 거래내역을 기록합니다 (출금/입금 쌍).
     */
    @Transactional
    public void recordTransfer(
            Long fromAccountId,
            Long toAccountId,
            String toAccountNumber,
            Long amount,
            Long fee,
            Long fromBalanceAfter,
            Long toBalanceAfter,
            String transactionId
    ) {
        // 출금 Activity
        recordTransferOut(
                fromAccountId,
                toAccountId,
                toAccountNumber,
                amount,
                fee,
                fromBalanceAfter,
                transactionId
        );

        // 입금 Activity
        // 입금자에게도 상대방 계좌 정보를 표시하도록 수정
        recordTransferIn(
                toAccountId,
                fromAccountId,
                toAccountNumber,  // 출금자의 계좌번호
                amount,
                toBalanceAfter,
                transactionId
        );
    }
}

