package com.leesuchan.account.domain.model.vo;

import com.leesuchan.account.config.AccountLimitProvider;
import jakarta.persistence.Embeddable;

/**
 * 일일 이체 한도 추적 Value Object
 */
@Embeddable
public class TransferLimitTracker extends DailyLimitTracker {

    /**
     * JPA 기본 생성자 (protected)
     */
    protected TransferLimitTracker() {
        super(AccountLimitProvider.getDailyTransferLimit());
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
                limitField.set(this, AccountLimitProvider.getDailyTransferLimit());
            } catch (Exception e) {
                throw new RuntimeException("TransferLimitTracker 초기화 실패", e);
            }
        }
        super.resetIfNeeded();
    }

    /**
     * 팩토리 메서드: 이체 한도 추적기 생성
     */
    public static TransferLimitTracker create() {
        return new TransferLimitTracker();
    }
}
