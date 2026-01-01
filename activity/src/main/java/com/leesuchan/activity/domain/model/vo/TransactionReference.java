package com.leesuchan.activity.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 이체 참조 정보 Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionReference {

    /**
     * 참조 계좌 ID
     */
    @Column(name = "reference_account_id")
    private Long accountId;

    /**
     * 참조 계좌번호
     */
    @Column(name = "reference_account_number", length = 20)
    private String accountNumber;

    /**
     * 생성자 (패키지 private)
     */
    TransactionReference(Long accountId, String accountNumber) {
        if (accountId == null) {
            throw new IllegalArgumentException("참조 계좌 ID는 필수입니다.");
        }
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("참조 계좌번호는 필수입니다.");
        }
        this.accountId = accountId;
        this.accountNumber = accountNumber;
    }

    /**
     * 팩토리 메서드
     */
    public static TransactionReference of(Long accountId, String accountNumber) {
        return new TransactionReference(accountId, accountNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionReference that = (TransactionReference) o;
        return Objects.equals(accountId, that.accountId) &&
               Objects.equals(accountNumber, that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, accountNumber);
    }

    @Override
    public String toString() {
        return "TransactionReference{" +
                "accountId=" + accountId +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}
