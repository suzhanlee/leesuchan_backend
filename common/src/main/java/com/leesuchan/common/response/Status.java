package com.leesuchan.common.response;

/**
 * API 응답 상태
 */
public class Status {
    private final boolean success;
    private final String code;
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
