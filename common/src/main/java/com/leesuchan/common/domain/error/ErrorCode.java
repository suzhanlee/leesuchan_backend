package com.leesuchan.common.domain.error;

/**
 * 에러 코드 인터페이스
 * 모든 에러 코드는 이 인터페이스를 구현하며, const로 관리됩니다.
 */
public interface ErrorCode {
    String getCode();
    String getMessage();
}
