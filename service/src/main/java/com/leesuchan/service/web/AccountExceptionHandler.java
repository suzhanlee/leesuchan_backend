package com.leesuchan.service.web;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 계좌 관련 예외 처리기 (404 응답)
 */
@RestControllerAdvice
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
}
