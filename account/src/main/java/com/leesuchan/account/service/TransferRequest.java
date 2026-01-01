package com.leesuchan.account.service;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 이체 요청 DTO (내부 사용)
 */
public record TransferRequest(
        @NotBlank(message = "출금 계좌번호는 필수입니다.")
        String fromAccountNumber,

        @NotBlank(message = "입금 계좌번호는 필수입니다.")
        String toAccountNumber,

        @Min(value = 1, message = "금액은 0보가 커야 합니다.")
        Long amount
) {
}
