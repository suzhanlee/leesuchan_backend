package com.leesuchan.service.application;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.service.dto.response.AccountResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계좌 조회 Query Service (CQRS)
 */
@Service
public class GetAccountQueryService {

    private final AccountRepository accountRepository;

    public GetAccountQueryService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 계좌번호로 계좌를 조회합니다.
     *
     * @param accountNumber 계좌번호
     * @return 계좌 응답 DTO
     */
    @Transactional(readOnly = true)
    public AccountResponse execute(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(AccountNotFoundException::new);

        return AccountResponse.from(account);
    }
}
