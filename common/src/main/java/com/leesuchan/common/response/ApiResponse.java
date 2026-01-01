package com.leesuchan.common.response;

import com.leesuchan.common.domain.error.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 공통 API 응답 wrapper
 */
@Schema(description = "공통 API 응답 wrapper")
public record ApiResponse<T>(
        @Schema(description = "응답 상태")
        Status status,

        @Schema(description = "응답 데이터")
        T data,

        @Schema(description = "추가 메시지")
        String message
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Status.success(), data, null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(Status.success(), null, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                Status.error(errorCode.getCode()),
                null,
                errorCode.getMessage()
        );
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(
                Status.error(code, message),
                null,
                message
        );
    }
}
