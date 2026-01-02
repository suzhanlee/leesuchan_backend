package com.leesuchan.account.config;

/**
 * 계좌 한도 설정 제공자
 *
 * <p>AccountProperties에서 설정을 받아 정적으로 제공합니다.
 * JPA Embeddable 클래스에서 설정에 접근하기 위해 사용합니다.
 */
public class AccountLimitProvider {

    /**
     * 일일 출금 한도 (원 단위)
     */
    private static Long dailyWithdrawLimit = 1_000_000L;

    /**
     * 일일 이체 한도 (원 단위)
     */
    private static Long dailyTransferLimit = 3_000_000L;

    /**
     * 이체 수수료율 (0.01 = 1%)
     */
    private static Double transferFeeRate = 0.01;

    /**
     * 설정을 초기화합니다. (AccountConfig에서 호출)
     */
    public static void initialize(Long dailyWithdrawLimit, Long dailyTransferLimit, Double transferFeeRate) {
        AccountLimitProvider.dailyWithdrawLimit = dailyWithdrawLimit;
        AccountLimitProvider.dailyTransferLimit = dailyTransferLimit;
        AccountLimitProvider.transferFeeRate = transferFeeRate;
    }

    public static Long getDailyWithdrawLimit() {
        return dailyWithdrawLimit;
    }

    public static Long getDailyTransferLimit() {
        return dailyTransferLimit;
    }

    public static Double getTransferFeeRate() {
        return transferFeeRate;
    }
}
