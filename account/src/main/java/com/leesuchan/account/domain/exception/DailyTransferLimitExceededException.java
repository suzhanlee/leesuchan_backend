package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.DomainException;

/**
 * 일일 이체 한도 초과 예외
 */
public class DailyTransferLimitExceededException extends DomainException {

    public DailyTransferLimitExceededException() {
        super(AccountErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED);
    }
}
