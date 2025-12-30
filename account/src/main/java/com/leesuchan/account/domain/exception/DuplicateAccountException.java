package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.DomainException;

/**
 * 중복 계좌번호일 때 발생하는 예외
 */
public class DuplicateAccountException extends DomainException {

    public DuplicateAccountException() {
        super(AccountErrorCode.DUPLICATE);
    }
}
