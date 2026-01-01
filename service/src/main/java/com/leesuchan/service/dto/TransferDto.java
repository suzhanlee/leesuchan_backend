package com.leesuchan.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 이체 요청 DTO
 */
@Schema(description = "이체 요청")
public record TransferDto(
        @Schema(description = "출금 계좌번호", example = "1234567890", required = true)
        @NotBlank(message = "출금 계좌번호는 필수입니다.")
        String fromAccountNumber,

        @Schema(description = "입금 계좌번호", example = "9876543210", required = true)
        @NotBlank(message = "입금 계좌번호는 필수입니다.")
        String toAccountNumber,

        @Schema(description = "이체 금액 (원 단위)", example = "10000", minimum = "1", required = true)
        @Min(value = 1, message = "금액은 0보가 커야 합니다.")
        Long amount
) {
}
