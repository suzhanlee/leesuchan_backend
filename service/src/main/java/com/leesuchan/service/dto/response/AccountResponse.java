package com.leesuchan.service.dto.response;

import com.leesuchan.account.domain.model.Account;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 계좌 응답 DTO
 */
@Schema(description = "계좌 응답")
public record AccountResponse(
        @Schema(description = "계좌 ID", example = "1")
        Long id,

        @Schema(description = "계좌번호", example = "1234567890")
        String accountNumber,

        @Schema(description = "계좌명", example = "홍길동")
        String accountName,

        @Schema(description = "잔액 (원 단위)", example = "100000")
        Long balance,

        @Schema(description = "생성일시", example = "2026-01-01T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정일시", example = "2026-01-01T12:30:00")
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
