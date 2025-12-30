package com.leesuchan.infra.database.repository;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Account Repository 구현체
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpaRepository;

    /**
     * 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
     */
    public AccountRepositoryImpl(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Account save(Account account) {
        return jpaRepository.save(account);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return jpaRepository.findByAccountNumberAndDeletedAtIsNull(accountNumber);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return jpaRepository.existsByAccountNumberAndDeletedAtIsNull(accountNumber);
    }

    @Override
    public void deleteByAccountNumber(String accountNumber) {
        Account account = jpaRepository.findByAccountNumberAndDeletedAtIsNull(accountNumber)
                .orElseThrow(AccountNotFoundException::new);
        account.delete();
        jpaRepository.save(account);
    }
}
