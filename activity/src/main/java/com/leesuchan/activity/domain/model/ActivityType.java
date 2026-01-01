package com.leesuchan.activity.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 거래 유형 enum
 */
@Schema(description = "거래 유형", allowableValues = {"DEPOSIT", "WITHDRAW", "TRANSFER_OUT", "TRANSFER_IN"})
public enum ActivityType {
    @Schema(description = "입금")
    DEPOSIT,

    @Schema(description = "출금")
    WITHDRAW,

    @Schema(description = "이체 (출금)")
    TRANSFER_OUT,

    @Schema(description = "이체 (입금)")
    TRANSFER_IN
}
