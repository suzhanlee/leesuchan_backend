package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.DomainException;

/**
 * 잔액 부족시 발생하는 예외
 */
public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException() {
        super(AccountErrorCode.INSUFFICIENT_BALANCE);
    }
}
