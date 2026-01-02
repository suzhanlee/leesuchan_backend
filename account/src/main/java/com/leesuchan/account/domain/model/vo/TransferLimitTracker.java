package com.leesuchan.account.domain.model.vo;

import com.leesuchan.account.config.AccountLimitProvider;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PostLoad;

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
     * JPA 로드 후 한도 값을 초기화합니다.
     * @Transient 필드는 DB에서 로드되지 않으므로 설정에서 값을 가져옵니다.
     */
    @PostLoad
    @Override
    protected void initLimit() {
        if (this.dailyLimit == null) {
            this.dailyLimit = AccountLimitProvider.getDailyTransferLimit();
        }
    }

    /**
     * 팩토리 메서드: 이체 한도 추적기 생성
     */
    public static TransferLimitTracker create() {
        return new TransferLimitTracker();
    }
}
