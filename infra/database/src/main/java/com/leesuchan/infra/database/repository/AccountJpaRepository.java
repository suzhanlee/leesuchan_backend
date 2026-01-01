package com.leesuchan.infra.database.repository;

import com.leesuchan.account.domain.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Account Spring Data JPA Repository
 */
public interface AccountJpaRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumberAndDeletedAtIsNull(String accountNumber);

    boolean existsByAccountNumberAndDeletedAtIsNull(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Account> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    Page<Account> findAllByDeletedAtIsNull(Pageable pageable);
}
