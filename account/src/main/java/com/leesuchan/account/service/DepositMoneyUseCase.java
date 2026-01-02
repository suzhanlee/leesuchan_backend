package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.activity.service.ActivityRecordService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 입금 유스케이스
 */
@Service
public class DepositMoneyUseCase {

    private final AccountRepository accountRepository;
    private final ActivityRecordService activityRecordService;

    public DepositMoneyUseCase(
            AccountRepository accountRepository,
            ActivityRecordService activityRecordService
    ) {
        this.accountRepository = accountRepository;
        this.activityRecordService = activityRecordService;
    }

    /**
     * 계좌에 금액을 입금합니다.
     *
     * @param accountNumber 계좌번호
     * @param amount 입금액
     * @return 입금된 계좌
     * @throws AccountNotFoundException 계좌 미조회 시
     */
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public Account execute(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(AccountNotFoundException::new);

        account.deposit(amount);
        accountRepository.save(account);

        activityRecordService.recordDeposit(
                account.getId(),
                amount,
                account.getBalance()
        );

        return account;
    }

    /**
     * DTO를 사용한 입금
     */
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public Account execute(DepositRequest request) {
        return execute(request.accountNumber(), request.amount());
    }
}
