package com.leesuchan.account.domain.model.vo;

import com.leesuchan.account.config.AccountLimitProvider;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PostLoad;

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
     * JPA 로드 후 한도 값을 초기화합니다.
     * @Transient 필드는 DB에서 로드되지 않으므로 설정에서 값을 가져옵니다.
     */
    @PostLoad
    @Override
    protected void initLimit() {
        if (this.dailyLimit == null) {
            this.dailyLimit = AccountLimitProvider.getDailyWithdrawLimit();
        }
    }

    /**
     * 팩토리 메서드: 출금 한도 추적기 생성
     */
    public static WithdrawLimitTracker create() {
        return new WithdrawLimitTracker();
    }
}
