package com.leesuchan.service;

import com.leesuchan.account.domain.exception.AccountNotFoundException;
import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.domain.repository.AccountRepository;
import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.repository.ActivityRepository;
import com.leesuchan.service.dto.ActivityResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 거래내역 조회 Query Service (CQRS)
 */
@Service
public class GetActivitiesQueryService {

    private final ActivityRepository activityRepository;
    private final AccountRepository accountRepository;

    public GetActivitiesQueryService(
            ActivityRepository activityRepository,
            AccountRepository accountRepository
    ) {
        this.activityRepository = activityRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * 계좌의 거래내역을 조회합니다.
     *
     * @param accountNumber 계좌번호
     * @return 거래내역 목록 (최신순)
     */
    @Transactional(readOnly = true)
    public List<ActivityResponse> execute(String accountNumber) {
        // 계좌 존재 확인
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(AccountNotFoundException::new);

        // 거래내역 조회 (최신순)
        List<Activity> activities = activityRepository
                .findByAccountIdOrderByCreatedAtDesc(account.getId());

        // Response DTO 변환
        return activities.stream()
                .map(ActivityResponse::from)
                .toList();
    }
}
