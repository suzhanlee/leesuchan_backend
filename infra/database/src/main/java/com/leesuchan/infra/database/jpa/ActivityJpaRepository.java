package com.leesuchan.infra.database.jpa;

import com.leesuchan.activity.domain.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Activity JPA Repository
 */
public interface ActivityJpaRepository extends JpaRepository<Activity, Long> {

    @Query("SELECT a FROM Activity a WHERE a.accountId = :accountId ORDER BY a.createdAt DESC")
    List<Activity> findByAccountId(@Param("accountId") Long accountId);
}
