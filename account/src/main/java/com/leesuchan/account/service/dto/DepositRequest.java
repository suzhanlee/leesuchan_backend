package com.leesuchan.account.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 입금 요청 DTO (내부 사용)
 */
public record DepositRequest(
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber,

        @Min(value = 1, message = "금액은 0보가 커야 합니다.")
        Long amount
) {
}
