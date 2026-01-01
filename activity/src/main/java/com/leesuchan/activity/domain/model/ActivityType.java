package com.leesuchan.activity.domain.model;

/**
 * 거래 유형 enum
 */
public enum ActivityType {
    DEPOSIT,        // 입금
    WITHDRAW,       // 출금
    TRANSFER_OUT,   // 이체 (출금)
    TRANSFER_IN     // 이체 (입금)
}
