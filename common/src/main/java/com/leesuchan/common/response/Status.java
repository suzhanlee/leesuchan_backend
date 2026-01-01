package com.leesuchan.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * API 응답 상태
 */
@Schema(description = "API 응답 상태")
public class Status {
    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 코드", example = "SUCCESS")
    private final String code;

    @Schema(description = "응답 메시지", example = "성공")
    private final String message;

    private Status(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static Status success() {
        return new Status(true, "SUCCESS", "성공");
    }

    public static Status error(String code, String message) {
        return new Status(false, code, message);
    }

    public static Status error(String code) {
        return new Status(false, code, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
