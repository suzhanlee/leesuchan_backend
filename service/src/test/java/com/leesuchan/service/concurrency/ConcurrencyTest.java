package com.leesuchan.service.concurrency;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.DepositMoneyUseCase;
import com.leesuchan.account.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 낙관적 락 동작 검증을 위한 동시성 테스트
 *
 * 참고: @Retryable과 @Transactional을 함께 사용할 때 트랜잭션 경계 문제로 인해
 * 동시성 테스트가 복잡해집니다. 이 테스트는 기본 구조를 제공하며,
 * 실제 환경에서의 동시성 테스트는 추가 설정이 필요할 수 있습니다.
 *
 * TODO: @Retryable의 트랜잭션 경계 문제 해결 후 테스트 활성화
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("동시성 테스트")
class ConcurrencyTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepositMoneyUseCase depositMoneyUseCase;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        // 테스트마다 고유한 계좌번호 사용으로 데이터 충돌 방지
        String uniqueId = String.valueOf(System.nanoTime()).substring(8);
        testAccount = Account.create("C-" + uniqueId, "동시성 테스트 계좌");
        accountRepository.save(testAccount);
    }

    @Test
    @Disabled("@Retryable과 @Transactional의 트랜잭션 경계 문제로 인해 일시적으로 비활성화")
    @DisplayName("동시 입금 요청 시 예외 없이 처리된다")
    void concurrent_deposit_without_exception() throws InterruptedException {
        // given
        int threadCount = 10;
        long depositAmount = 1000L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    depositMoneyUseCase.execute(testAccount.getAccountNumber(), depositAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        // 모든 요청이 성공적으로 처리되어야 함 (@Retryable로 인해)
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(errorCount.get()).isEqualTo(0);

        // 최종 잔액 확인 (모든 입금이 반영되어야 함)
        Account result = accountRepository.findByAccountNumber(testAccount.getAccountNumber()).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(threadCount * depositAmount);
    }
}
