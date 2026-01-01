package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.DomainException;

/**
 * 동일 계좌 이체 예외
 */
public class SameAccountTransferException extends DomainException {

    public SameAccountTransferException() {
        super(AccountErrorCode.SAME_ACCOUNT_TRANSFER);
    }
}
