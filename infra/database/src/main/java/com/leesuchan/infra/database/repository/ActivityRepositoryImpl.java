package com.leesuchan.infra.database.repository;

import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Activity Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ActivityRepositoryImpl implements ActivityRepository {

    private final ActivityJpaRepository jpaRepository;

    @Override
    public Activity save(Activity activity) {
        return jpaRepository.save(activity);
    }

    @Override
    public List<Activity> findByAccountId(Long accountId) {
        return jpaRepository.findByAccountId(accountId);
    }

    @Override
    public List<Activity> findByAccountIdOrderByCreatedAtDesc(Long accountId) {
        return jpaRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}
