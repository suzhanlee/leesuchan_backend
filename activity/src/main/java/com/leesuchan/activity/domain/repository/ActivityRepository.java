package com.leesuchan.activity.domain.repository;

import com.leesuchan.activity.domain.model.Activity;

import java.util.List;

/**
 * Activity Repository Port 인터페이스
 */
public interface ActivityRepository {

    /**
     * Activity를 저장합니다.
     */
    Activity save(Activity activity);

    /**
     * 특정 계좌의 모든 거래내역을 조회합니다.
     */
    List<Activity> findByAccountId(Long accountId);

    /**
     * 특정 계좌의 거래내역을 최신순으로 조회합니다.
     */
    List<Activity> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
