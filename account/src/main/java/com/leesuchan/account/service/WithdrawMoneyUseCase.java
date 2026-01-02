package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.account.service.dto.WithdrawRequest;
import com.leesuchan.activity.service.ActivityRecordService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 출금 유스케이스
 */
@Service
public class WithdrawMoneyUseCase {

    private final AccountRepository accountRepository;
    private final ActivityRecordService activityRecordService;

    public WithdrawMoneyUseCase(
            AccountRepository accountRepository,
            ActivityRecordService activityRecordService
    ) {
        this.accountRepository = accountRepository;
        this.activityRecordService = activityRecordService;
    }

    /**
     * 계좌에서 금액을 출금합니다.
     *
     * @param accountNumber 계좌번호
     * @param amount 출금액
     * @return 출금된 계좌
     * @throws AccountNotFoundException 계좌 미조회 시
     */
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public Account execute(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(AccountNotFoundException::new);

        account.withdraw(amount);
        accountRepository.save(account);

        activityRecordService.recordWithdraw(
                account.getId(),
                amount,
                account.getBalance()
        );

        return account;
    }

    /**
     * DTO를 사용한 출금
     */
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public Account execute(WithdrawRequest request) {
        return execute(request.accountNumber(), request.amount());
    }
}
