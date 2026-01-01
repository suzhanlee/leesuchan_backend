package com.leesuchan.service.dto;

/**
 * 이체 응답 DTO
 */
public record TransferResponse(
        AccountResponse fromAccount,
        AccountResponse toAccount,
        Long fee
) {
}
