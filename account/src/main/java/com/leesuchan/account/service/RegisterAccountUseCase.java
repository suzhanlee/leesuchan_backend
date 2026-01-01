package com.leesuchan.account.service;

import com.leesuchan.account.domain.exception.DuplicateAccountException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계좌 등록 유스케이스
 */
@Service
public class RegisterAccountUseCase {

    private final AccountRepository accountRepository;

    /**
     * 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
     */
    public RegisterAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 계좌를 등록합니다.
     *
     * @param accountNumber 계좌번호
     * @param accountName  계좌명
     * @return 등록된 계좌
     * @throws DuplicateAccountException 중복 계좌번호일 경우
     */
    @Transactional
    public Account execute(String accountNumber, String accountName) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new DuplicateAccountException();
        }

        Account account = Account.create(accountNumber, accountName);
        return accountRepository.save(account);
    }

    /**
     * DTO를 사용한 계좌 등록
     */
    @Transactional
    public Account execute(RegisterAccountRequest request) {
        return execute(request.accountNumber(), request.accountName());
    }
}
