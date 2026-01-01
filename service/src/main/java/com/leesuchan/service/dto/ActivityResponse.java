package com.leesuchan.service.dto;

import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.model.ActivityType;

import java.time.LocalDateTime;

/**
 * 거래내역 응답 DTO
 */
public record ActivityResponse(
        Long id,
        ActivityType activityType,
        Long amount,
        Long fee,
        Long balanceAfter,
        String referenceAccountNumber,
        String description,
        LocalDateTime createdAt
) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getAmount(),
                activity.getFee(),
                activity.getBalanceAfter(),
                activity.getReferenceAccountNumber(),
                activity.getDescription(),
                activity.getCreatedAt()
        );
    }
}
