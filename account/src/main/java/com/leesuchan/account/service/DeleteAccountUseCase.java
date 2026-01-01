package com.leesuchan.account.service;

import com.leesuchan.account.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계좌 삭제 유스케이스
 */
@Service
public class DeleteAccountUseCase {

    private final AccountRepository accountRepository;

    public DeleteAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 계좌를 삭제합니다 (소프트 삭제).
     *
     * @param accountNumber 삭제할 계좌번호
     */
    @Transactional
    public void execute(String accountNumber) {
        accountRepository.deleteByAccountNumber(accountNumber);
    }
}
