package com.leesuchan.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 출금 요청 DTO
 */
public record WithdrawDto(
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber,

        @Min(value = 1, message = "금액은 0보가 커야 합니다.")
        Long amount
) {
}
