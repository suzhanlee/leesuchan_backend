package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.service.dto.AccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계좌 목록 조회 Query Service (CQRS)
 */
@Service
public class GetAccountsQueryService {

    private final AccountRepository accountRepository;

    public GetAccountsQueryService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 계좌 목록을 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 계좌 응답 DTO 페이지
     */
    @Transactional(readOnly = true)
    public Page<AccountResponse> execute(Pageable pageable) {
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(AccountResponse::from);
    }
}
