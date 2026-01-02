package com.leesuchan.service.web;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.common.response.ApiResponse;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 계좌 관련 예외 처리기 (404 응답)
 */
@RestControllerAdvice
@Order(1)
public class AccountExceptionHandler {

    /**
     * 계좌 미조회 예외 처리 (404)
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFoundException(AccountNotFoundException e) {
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getErrorCode()));
    }

    /**
     * 낙관적 락 충돌 예외 처리
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        return ResponseEntity
                .status(org.springframework.http.HttpStatus.CONFLICT)
                .body(ApiResponse.error("OPTIMISTIC_LOCK_CONFLICT", "다른 요청과 충돌이 발생했습니다. 다시 시도해주세요."));
    }
}
