package com.leesuchan.service.dto.response;

import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.model.ActivityType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 거래내역 응답 DTO
 */
@Schema(description = "거래내역 응답")
public record ActivityResponse(
        @Schema(description = "거래 ID", example = "1")
        Long id,

        @Schema(description = "거래 유형", example = "DEPOSIT")
        ActivityType activityType,

        @Schema(description = "거래 금액 (원 단위)", example = "10000")
        Long amount,

        @Schema(description = "수수료 (원 단위)", example = "0")
        Long fee,

        @Schema(description = "거래 후 잔액 (원 단위)", example = "10000")
        Long balanceAfter,

        @Schema(description = "상대방 계좌번호 (이체 시)", example = "9876543210")
        String referenceAccountNumber,

        @Schema(description = "메모", example = "월급 이체")
        String description,

        @Schema(description = "거래 일시", example = "2026-01-01T12:00:00")
        LocalDateTime createdAt
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getAmount(),
                activity.getFee(),
                activity.getBalanceAfter(),
                activity.getTransactionReference() != null ? activity.getTransactionReference().getAccountNumber() : null,
                activity.getDescription(),
                activity.getCreatedAt()
        );
    }
}
