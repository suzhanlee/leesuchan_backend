package com.leesuchan.account.domain.model.vo;

import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * 일일 한도 추적 Value Object
 */
@Getter
public abstract class DailyLimitTracker {

    /**
     * 현재까지 누적된 금액
     */
    protected Long accumulatedAmount = 0L;

    /**
     * 마지막 트랜잭션 날짜
     */
    protected LocalDate lastTransactionDate;

    /**
     * 일일 한도 (DB에 저장되지 않음)
     */
    @Transient
    protected Long dailyLimit;

    /**
     * 생성자 (패키지 private)
     */
    protected DailyLimitTracker(Long dailyLimit) {
        if (dailyLimit == null || dailyLimit <= 0) {
            throw new IllegalArgumentException("일일 한도는 0보다 커야 합니다.");
        }
        this.dailyLimit = dailyLimit;
        this.accumulatedAmount = 0L;
        this.lastTransactionDate = null;
    }

    /**
     * JPA 로드 후 한도 값을 초기화합니다.
     * @Transient 필드는 DB에서 로드되지 않으므로 서브클래스에서 오버라이드하여 초기화합니다.
     */
    @PostLoad
    protected void initLimit() {
        // 서브클래스에서 오버라이드하여 구현
    }

    /**
     * 금액을 추가하고 한도를 체크합니다.
     *
     * @param amount 추가할 금액
     * @param exceptionSupplier 한도 초과 시 발생시킬 예외 Supplier
     * @param <T> 예외 타입
     * @throws T 한도 초과 시 발생하는 예외
     */
    public <T extends Throwable> void trackAndCheckLimit(long amount, Supplier<? extends T> exceptionSupplier) throws T {
        if (amount <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
        }

        resetIfNeeded();

        if (this.accumulatedAmount + amount > this.dailyLimit) {
            throw exceptionSupplier.get();
        }

        this.accumulatedAmount += amount;
        this.lastTransactionDate = LocalDate.now();
    }

    /**
     * 날짜가 바뀌었으면 한도를 리셋합니다.
     */
    protected void resetIfNeeded() {
        LocalDate today = LocalDate.now();
        if (this.lastTransactionDate == null || !this.lastTransactionDate.equals(today)) {
            this.accumulatedAmount = 0L;
            this.lastTransactionDate = today;
        }
    }

    /**
     * 현재 누적 금액을 반환합니다. (리셋 없이)
     */
    public Long getAccumulatedAmount() {
        return this.accumulatedAmount;
    }
}
