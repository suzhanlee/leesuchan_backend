package com.leesuchan.service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 출금 요청 DTO
 */
@Schema(description = "출금 요청")
public record WithdrawDto(
        @Schema(description = "계좌번호", example = "1234567890", required = true)
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber,

        @Schema(description = "출금 금액 (원 단위)", example = "5000", minimum = "1", required = true)
        @Min(value = 1, message = "금액은 0보가 커야 합니다.")
        Long amount
) {
}
