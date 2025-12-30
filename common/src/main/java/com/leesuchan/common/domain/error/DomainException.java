package com.leesuchan.common.domain.error;

/**
 * 도메인 예외 기반 클래스
 * 모든 도메인 예외는 이 클래스를 상속받습니다.
 */
public abstract class DomainException extends RuntimeException {
    private final ErrorCode errorCode;

    protected DomainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
