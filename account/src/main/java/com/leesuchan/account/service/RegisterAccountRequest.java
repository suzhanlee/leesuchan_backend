package com.leesuchan.account.service;

/**
 * 계좌 등록 요청 DTO (내부 사용)
 */
public record RegisterAccountRequest(
        String accountNumber,
        String accountName
) {
}
