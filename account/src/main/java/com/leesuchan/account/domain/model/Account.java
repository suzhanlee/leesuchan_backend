package com.leesuchan.account.domain.model;

import com.leesuchan.account.domain.exception.DailyWithdrawLimitExceededException;
import com.leesuchan.account.domain.exception.InsufficientBalanceException;
import com.leesuchan.account.domain.exception.InvalidAccountNameException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 계좌 Entity (JPA)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account", indexes = {
        @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "account_name", nullable = false, length = 100)
    private String accountName;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "daily_withdraw_amount", nullable = false)
    private Long dailyWithdrawAmount = 0L;

    @Column(name = "daily_transfer_amount", nullable = false)
    private Long dailyTransferAmount = 0L;

    @Column(name = "last_withdraw_date")
    private LocalDate lastWithdrawDate;

    @Column(name = "last_transfer_date")
    private LocalDate lastTransferDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    private Long version;

    // 생성자 (패키지 private)
    Account(String accountNumber, String accountName, Long balance) {
        validateAccountNumber(accountNumber);
        if (accountName == null || accountName.isBlank()) {
            throw new InvalidAccountNameException("계좌명은 비어있을 수 없습니다.");
        }
        if (accountName.length() > 100) {
            throw new InvalidAccountNameException("계좌명은 100자 이하여야 합니다.");
        }
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    /**
     * 계좌 생성 팩토리 메서드
     */
    public static Account create(String accountNumber, String accountName) {
        return new Account(accountNumber, accountName, 0L);
    }

    /**
     * 계좌번호 유효성 검증
     */
    private static void validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 비어있을 수 없습니다.");
        }
        if (accountNumber.length() < 3 || accountNumber.length() > 20) {
            throw new IllegalArgumentException("계좌번호는 3~20자여야 합니다.");
        }
    }

    /**
     * 입금
     */
    public void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 출금
     */
    public void withdraw(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }
        checkDailyWithdrawLimit(amount);
        checkSufficientBalance(amount);

        this.balance -= amount;
        this.dailyWithdrawAmount += amount;
        this.lastWithdrawDate = LocalDate.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 일일 출금 한도 체크 (1,000,000원)
     */
    private void checkDailyWithdrawLimit(long amount) {
        LocalDate today = LocalDate.now();
        // 날짜가 바뀌면 한도 리셋
        if (this.lastWithdrawDate == null || !this.lastWithdrawDate.equals(today)) {
            this.dailyWithdrawAmount = 0L;
            this.lastWithdrawDate = today;
        }
        // 누적 + 현재 금액이 한도 초과 시 예외
        if (this.dailyWithdrawAmount + amount > 1_000_000L) {
            throw new DailyWithdrawLimitExceededException();
        }
    }

    /**
     * 잔액 확인
     */
    private void checkSufficientBalance(long amount) {
        if (this.balance < amount) {
            throw new InsufficientBalanceException();
        }
    }

    /**
     * 소프트 삭제
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
