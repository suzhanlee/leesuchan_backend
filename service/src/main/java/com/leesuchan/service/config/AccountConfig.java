package com.leesuchan.service.config;

import com.leesuchan.account.config.AccountLimitProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 계좌 설정 초기화
 *
 * <p>AccountProperties를 읽어 AccountLimitProvider를 초기화합니다.
 */
@Component
@RequiredArgsConstructor
public class AccountConfig {

    private final AccountProperties accountProperties;

    @PostConstruct
    public void init() {
        AccountLimitProvider.initialize(
                accountProperties.getLimits().getDailyWithdraw(),
                accountProperties.getLimits().getDailyTransfer(),
                accountProperties.getFees().getTransferRate()
        );
    }
}
