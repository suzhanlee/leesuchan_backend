package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.DomainException;

/**
 * 계좌명이 유효하지 않을 때 발생하는 예외
 */
public class InvalidAccountNameException extends DomainException {

    public InvalidAccountNameException(String message) {
        super(AccountErrorCode.INVALID_NAME);
    }
}
