package com.leesuchan.service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 이체 응답 DTO
 */
@Schema(description = "이체 응답")
public record TransferResponse(
        @Schema(description = "출금 계좌 정보")
        AccountResponse fromAccount,

        @Schema(description = "입금 계좌 정보")
        AccountResponse toAccount,

        @Schema(description = "이체 수수료 (원 단위)", example = "100")
        Long fee
) {
}
