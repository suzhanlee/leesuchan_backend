package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.DomainException;

/**
 * 일일 출금 한도 초과 예외
 */
public class DailyWithdrawLimitExceededException extends DomainException {

    public DailyWithdrawLimitExceededException() {
        super(AccountErrorCode.DAILY_WITHDRAW_LIMIT_EXCEEDED);
    }
}
