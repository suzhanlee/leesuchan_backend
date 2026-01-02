package com.leesuchan.account.domain.model.vo;

import com.leesuchan.account.config.AccountLimitProvider;
import jakarta.persistence.Embeddable;

/**
 * 일일 출금 한도 추적 Value Object
 */
@Embeddable
public class WithdrawLimitTracker extends DailyLimitTracker {

    /**
     * JPA 기본 생성자 (protected)
     */
    protected WithdrawLimitTracker() {
        super(AccountLimitProvider.getDailyWithdrawLimit());
    }

    /**
     * JPA 기본 생성자용 초기화 메서드
     */
    @Override
    protected void resetIfNeeded() {
        if (this.dailyLimit == null) {
            // JPA 로딩 시 dailyLimit가 null이면 초기화
            try {
                java.lang.reflect.Field limitField = DailyLimitTracker.class.getDeclaredField("dailyLimit");
                limitField.setAccessible(true);
                limitField.set(this, AccountLimitProvider.getDailyWithdrawLimit());
            } catch (Exception e) {
                throw new RuntimeException("WithdrawLimitTracker 초기화 실패", e);
            }
        }
        super.resetIfNeeded();
    }

    /**
     * 팩토리 메서드: 출금 한도 추적기 생성
     */
    public static WithdrawLimitTracker create() {
        return new WithdrawLimitTracker();
    }
}
