package com.leesuchan.account.domain.exception;

import com.leesuchan.common.domain.error.ErrorCode;

/**
 * 계좌 관련 에러 코드
 */
public interface AccountErrorCode extends ErrorCode {

    ErrorCode NOT_FOUND = of("ACCOUNT_001", "계좌를 찾을 수 없습니다.");
    ErrorCode DUPLICATE = of("ACCOUNT_002", "이미 존재하는 계좌번호입니다.");
    ErrorCode INVALID_NAME = of("ACCOUNT_003", "계좌명이 유효하지 않습니다.");
    ErrorCode INSUFFICIENT_BALANCE = of("ACCOUNT_004", "잔액이 부족합니다.");
    ErrorCode DAILY_WITHDRAW_LIMIT_EXCEEDED = of("ACCOUNT_005", "일일 출금 한도를 초과했습니다. (1,000,000원)");
    ErrorCode DAILY_TRANSFER_LIMIT_EXCEEDED = of("ACCOUNT_006", "일일 이체 한도를 초과했습니다. (3,000,000원)");
    ErrorCode SAME_ACCOUNT_TRANSFER = of("ACCOUNT_007", "동일 계좌로 이체할 수 없습니다.");

    static ErrorCode of(String code, String message) {
        return new ErrorCode() {
            @Override
            public String getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }
}
