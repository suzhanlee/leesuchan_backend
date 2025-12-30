package com.leesuchan.service.dto;

import com.leesuchan.account.domain.model.Account;

import java.time.LocalDateTime;

/**
 * 계좌 응답 DTO
 */
public record AccountResponse(
        Long id,
        String accountNumber,
        String accountName,
        Long balance,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
