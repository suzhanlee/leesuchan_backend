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
        Activity withdrawActivity = Activity.transferOut(
                fromAccountId,
                amount,
                fee,
                fromBalanceAfter,
                transactionId,
                toAccountId,
                toAccountNumber
        );
        activityRepository.save(withdrawActivity);

        // 입금 Activity
        Activity depositActivity = Activity.transferIn(
                toAccountId,
                amount,
                toBalanceAfter,
                transactionId,
                fromAccountId,
                null  // 상대방 계좌번호는 입금자에게 표시하지 않음
        );
        activityRepository.save(depositActivity);
    }
}
