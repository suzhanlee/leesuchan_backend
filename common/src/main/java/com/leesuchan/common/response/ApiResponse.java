package com.leesuchan.common.response;

import com.leesuchan.common.domain.error.ErrorCode;

/**
 * 공통 API 응답 wrapper
 */
public record ApiResponse<T>(
        Status status,
        T data,
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
