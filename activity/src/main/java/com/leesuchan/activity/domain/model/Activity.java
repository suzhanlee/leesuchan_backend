package com.leesuchan.activity.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 거래내역 Entity (JPA)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "fee", nullable = false)
    private Long fee;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "reference_account_id")
    private Long referenceAccountId;

    @Column(name = "reference_account_number", length = 20)
    private String referenceAccountNumber;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성자 (패키지 private)
    Activity(
            Long accountId,
            ActivityType activityType,
            Long amount,
            Long fee,
            Long balanceAfter,
            Long referenceAccountId,
            String referenceAccountNumber,
            String description,
            String transactionId
    ) {
        this.accountId = accountId;
        this.activityType = activityType;
        this.amount = amount;
        this.fee = fee;
        this.balanceAfter = balanceAfter;
        this.referenceAccountId = referenceAccountId;
        this.referenceAccountNumber = referenceAccountNumber;
        this.description = description;
        this.transactionId = transactionId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 입금 Activity 생성 팩토리 메서드
     */
    public static Activity deposit(Long accountId, Long amount, Long balanceAfter) {
        return new Activity(
                accountId,
                ActivityType.DEPOSIT,
                amount,
                0L,  // fee = 0
                balanceAfter,
                null,  // referenceAccountId
                null,  // referenceAccountNumber
                null,  // description
                null   // transactionId
        );
    }

    /**
     * 출금 Activity 생성 팩토리 메서드
     */
    public static Activity withdraw(Long accountId, Long amount, Long balanceAfter) {
        return new Activity(
                accountId,
                ActivityType.WITHDRAW,
                amount,
                0L,  // fee = 0
                balanceAfter,
                null,  // referenceAccountId
                null,  // referenceAccountNumber
                null,  // description
                null   // transactionId
        );
    }

    /**
     * 이체 Activity 생성 팩토리 메서드 (출금)
     */
    public static Activity transferOut(
            Long accountId,
            Long referenceAccountId,
            String referenceAccountNumber,
            Long amount,
            Long fee,
            Long balanceAfter,
            String transactionId
    ) {
        return new Activity(
                accountId,
                ActivityType.TRANSFER_OUT,
                amount,
                fee,
                balanceAfter,
                referenceAccountId,
                referenceAccountNumber,
                null,  // description
                transactionId
        );
    }

    /**
     * 이체 Activity 생성 팩토리 메서드 (입금)
     */
    public static Activity transferIn(
            Long accountId,
            Long referenceAccountId,
            String referenceAccountNumber,
            Long amount,
            Long balanceAfter,
            String transactionId
    ) {
        return new Activity(
                accountId,
                ActivityType.TRANSFER_IN,
                amount,
                0L,  // fee = 입금자는 수수료 없음
                balanceAfter,
                referenceAccountId,
                referenceAccountNumber,
                null,  // description
                transactionId
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return Objects.equals(id, activity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
