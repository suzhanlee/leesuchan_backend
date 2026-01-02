package com.leesuchan.account.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 일일 출금 한도 추적 Value Object
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawLimitTracker {

    /**
     * 현재까지 누적된 출금 금액
     */
    @Column(name = "daily_withdraw_amount", nullable = false)
    private Long accumulatedAmount = 0L;

    /**
     * 마지막 출금 트랜잭션 날짜
     */
    @Column(name = "last_withdraw_date")
    private LocalDate lastTransactionDate;

    /**
     * 팩토리 메서드: 출금 한도 추적기 생성
     */
    public static WithdrawLimitTracker create() {
        return new WithdrawLimitTracker();
    }

    /**
     * 날짜가 바뀌었으면 한도를 리셋합니다.
     */
    public void resetIfNeeded() {
        LocalDate today = LocalDate.now();
        if (this.lastTransactionDate == null || !this.lastTransactionDate.equals(today)) {
            this.accumulatedAmount = 0L;
            this.lastTransactionDate = today;
        }
    }

    /**
     * 금액을 추가하고 누적 금액을 반환합니다.
     *
     * @param amount 추가할 금액
     * @return 업데이트된 누적 금액
     */
    public long trackAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }

        resetIfNeeded();

        this.accumulatedAmount += amount;
        this.lastTransactionDate = LocalDate.now();

        return this.accumulatedAmount;
    }

    /**
     * 현재 누적 금액을 반환합니다.
     */
    public long getAccumulatedAmount() {
        return this.accumulatedAmount;
    }
}
