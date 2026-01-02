package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.exception.SameAccountTransferException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.account.service.dto.TransferRequest;
import com.leesuchan.activity.service.ActivityRecordService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이체 유스케이스
 */
@Service
public class TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final ActivityRecordService activityRecordService;

    public TransferMoneyUseCase(
            AccountRepository accountRepository,
            ActivityRecordService activityRecordService
    ) {
        this.accountRepository = accountRepository;
        this.activityRecordService = activityRecordService;
    }

    /**
     * 계좌 간 이체를 실행합니다.
     *
     * @param fromAccountNumber 출금 계좌번호
     * @param toAccountNumber 입금 계좌번호
     * @param amount 이체 금액
     * @return TransferResult (from 계좌, to 계좌, 수수료)
     */
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public TransferResult execute(String fromAccountNumber, String toAccountNumber, Long amount) {
        // 1. 두 계좌 조회
        Account from = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(AccountNotFoundException::new);
        Account to = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(AccountNotFoundException::new);

        // 2. 동일 계좌 체크
        if (from.getId().equals(to.getId())) {
            throw new SameAccountTransferException();
        }

        // 3. 이체 수행 및 수수료 수신
        long fee = from.transfer(to, amount);

        // 4. 저장
        accountRepository.save(from);
        accountRepository.save(to);

        // 5. Activity 기록 (2개)
        String transactionId = generateTransactionId();
        activityRecordService.recordTransferOut(
                from.getId(),
                to.getId(),
                to.getAccountNumber(),
                amount,
                fee,
                from.getBalance(),
                transactionId
        );
        activityRecordService.recordTransferIn(
                to.getId(),
                from.getId(),
                from.getAccountNumber(),
                amount,
                to.getBalance(),
                transactionId
        );

        return new TransferResult(from, to, fee);
    }

    /**
     * DTO를 사용한 이체
     */
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3)
    public TransferResult execute(TransferRequest request) {
        return execute(request.fromAccountNumber(), request.toAccountNumber(), request.amount());
    }

    /**
     * 트랜잭션 ID 생성
     */
    private String generateTransactionId() {
        return "TX_" + System.currentTimeMillis();
    }

    /**
     * 이체 결과
     */
    public record TransferResult(Account from, Account to, Long fee) {
    }
}
