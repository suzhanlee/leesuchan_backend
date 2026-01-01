package com.leesuchan.account.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 계좌 등록 요청 DTO (내부 사용)
 */
public record RegisterAccountRequest(
        @NotBlank(message = "계좌번호는 필수입니다.")
        @Size(min = 3, max = 20, message = "계좌번호는 3~20자여야 합니다.")
        String accountNumber,

        @NotBlank(message = "계좌명은 필수입니다.")
        @Size(max = 100, message = "계좌명은 100자 이하여야 합니다.")
        String accountName
) {
}
