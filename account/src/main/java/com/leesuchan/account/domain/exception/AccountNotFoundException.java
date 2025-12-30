package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.DomainException;

/**
 * 계좌를 찾을 수 없을 때 발생하는 예외
 */
public class AccountNotFoundException extends DomainException {

    public AccountNotFoundException() {
        super(AccountErrorCode.NOT_FOUND);
    }

    public AccountNotFoundException(String message) {
        super(AccountErrorCode.NOT_FOUND);
    }
}
