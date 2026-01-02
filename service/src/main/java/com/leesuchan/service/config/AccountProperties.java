package com.leesuchan.service.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 계좌 관련 설정 Properties
 */
@Getter
@Component
@ConfigurationProperties(prefix = "account")
public class AccountProperties {

    /**
     * 한도 설정
     */
    private Limit limits = new Limit();

    /**
     * 수수료 설정
     */
    private Fee fees = new Fee();

    @Getter
    public static class Limit {
        /**
         * 일일 출금 한도 (원 단위)
         */
        private Long dailyWithdraw = 1_000_000L;

        /**
         * 일일 이체 한도 (원 단위)
         */
        private Long dailyTransfer = 3_000_000L;

        public void setDailyWithdraw(Long dailyWithdraw) {
            this.dailyWithdraw = dailyWithdraw;
        }

        public void setDailyTransfer(Long dailyTransfer) {
            this.dailyTransfer = dailyTransfer;
        }
    }

    @Getter
    public static class Fee {
        /**
         * 이체 수수료율 (0.01 = 1%)
         */
        private Double transferRate = 0.01;

        public void setTransferRate(Double transferRate) {
            this.transferRate = transferRate;
        }
    }
}
