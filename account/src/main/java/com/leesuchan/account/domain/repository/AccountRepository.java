package com.leesuchan.account.domain.repository;

import com.leesuchan.account.domain.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 계좌 Repository Port
 */
public interface AccountRepository {

    /**
     * 계좌 저장
     */
    Account save(Account account);

    /**
     * 계좌번호로 계좌 조회
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * ID로 계좌 조회
     */
    Optional<Account> findById(Long id);

    /**
     * 계좌번호 존재 여부 확인
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * 계좌 삭제 (소프트 삭제)
     */
    void deleteByAccountNumber(String accountNumber);

    /**
     * 계좌 목록 조회 (페이지네이션)
     */
    Page<Account> findAll(Pageable pageable);
}
