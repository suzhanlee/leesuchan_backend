package com.leesuchan.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 계좌 등록 요청 DTO
 */
@Schema(description = "계좌 등록 요청")
public record RegisterAccountDto(
        @Schema(description = "계좌번호", example = "1234567890", minLength = 3, maxLength = 20, required = true)
        @NotBlank(message = "계좌번호는 필수입니다.")
        @Size(min = 3, max = 20, message = "계좌번호는 3~20자여야 합니다.")
        String accountNumber,

        @Schema(description = "계좌명", example = "홍길동", maxLength = 100, required = true)
        @NotBlank(message = "계좌명은 필수입니다.")
        @Size(max = 100, message = "계좌명은 100자 이하여야 합니다.")
        String accountName
) {
}
