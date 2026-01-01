package com.leesuchan.account.domain.model.vo;

import com.leesuchan.account.domain.exception.DailyTransferLimitExceededException;
import com.leesuchan.account.domain.exception.DailyWithdrawLimitExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DailyLimitTracker 테스트")
class DailyLimitTrackerTest {

    @Test
    @DisplayName("출금 한도 추적기를 생성한다")
    void create_withdraw_limit_tracker() {
        // when
        WithdrawLimitTracker tracker = WithdrawLimitTracker.create();

        // then
        assertThat(tracker.getAccumulatedAmount()).isZero();
        assertThat(tracker.getLastTransactionDate()).isNull();
    }

    @Test
    @DisplayName("이체 한도 추적기를 생성한다")
    void create_transfer_limit_tracker() {
        // when
        TransferLimitTracker tracker = TransferLimitTracker.create();

        // then
        assertThat(tracker.getAccumulatedAmount()).isZero();
    }

    @Test
    @DisplayName("출금 한도 내에서 금액을 추적한다")
    void track_withdraw_amount_within_limit() {
        // given
        WithdrawLimitTracker tracker = WithdrawLimitTracker.create();

        // when
        tracker.trackAndCheckLimit(500_000L, DailyWithdrawLimitExceededException::new);

        // then
        assertThat(tracker.getAccumulatedAmount()).isEqualTo(500_000L);
        assertThat(tracker.getLastTransactionDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("이체 한도 내에서 금액을 추적한다")
    void track_transfer_amount_within_limit() {
        // given
        TransferLimitTracker tracker = TransferLimitTracker.create();

        // when
        tracker.trackAndCheckLimit(1_000_000L, DailyTransferLimitExceededException::new);

        // then
        assertThat(tracker.getAccumulatedAmount()).isEqualTo(1_000_000L);
    }

    @Test
    @DisplayName("출금 한도를 초과하면 예외가 발생한다")
    void track_withdraw_amount_exceeds_limit_throws_exception() {
        // given
        WithdrawLimitTracker tracker = WithdrawLimitTracker.create();

        // when & then
        assertThatThrownBy(() -> tracker.trackAndCheckLimit(1_000_001L, DailyWithdrawLimitExceededException::new))
                .isInstanceOf(DailyWithdrawLimitExceededException.class);
    }

    @Test
    @DisplayName("이체 한도를 초과하면 예외가 발생한다")
    void track_transfer_amount_exceeds_limit_throws_exception() {
        // given
        TransferLimitTracker tracker = TransferLimitTracker.create();

        // when & then
        assertThatThrownBy(() -> tracker.trackAndCheckLimit(3_000_001L, DailyTransferLimitExceededException::new))
                .isInstanceOf(DailyTransferLimitExceededException.class);
    }

    @Test
    @DisplayName("여러 번 출금해도 한도 내면 성공한다")
    void track_multiple_withdraws_within_limit() {
        // given
        WithdrawLimitTracker tracker = WithdrawLimitTracker.create();

        // when
        tracker.trackAndCheckLimit(500_000L, DailyWithdrawLimitExceededException::new);
        tracker.trackAndCheckLimit(400_000L, DailyWithdrawLimitExceededException::new);

        // then
        assertThat(tracker.getAccumulatedAmount()).isEqualTo(900_000L);
    }

    @Test
    @DisplayName("여러 번 이체해도 한도 내면 성공한다")
    void track_multiple_transfers_within_limit() {
        // given
        TransferLimitTracker tracker = TransferLimitTracker.create();

        // when
        tracker.trackAndCheckLimit(1_000_000L, DailyTransferLimitExceededException::new);
        tracker.trackAndCheckLimit(1_500_000L, DailyTransferLimitExceededException::new);

        // then
        assertThat(tracker.getAccumulatedAmount()).isEqualTo(2_500_000L);
    }

    @Test
    @DisplayName("날짜가 바뀌면 출금 한도가 리셋된다")
    void reset_withdraw_limit_when_day_changes() {
        // given
        WithdrawLimitTracker tracker = WithdrawLimitTracker.create();
        tracker.trackAndCheckLimit(500_000L, DailyWithdrawLimitExceededException::new);

        // 날짜 변경 시뮬레이션 (리플렉션 사용)
        try {
            java.lang.reflect.Field dateField = DailyLimitTracker.class.getDeclaredField("lastTransactionDate");
            dateField.setAccessible(true);
            dateField.set(tracker, LocalDate.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // when
        tracker.trackAndCheckLimit(1_000_000L, DailyWithdrawLimitExceededException::new);

        // then
        assertThat(tracker.getAccumulatedAmount()).isEqualTo(1_000_000L);
    }

    @Test
    @DisplayName("날짜가 바뀌면 이체 한도가 리셋된다")
    void reset_transfer_limit_when_day_changes() {
        // given
        TransferLimitTracker tracker = TransferLimitTracker.create();
        tracker.trackAndCheckLimit(2_000_000L, DailyTransferLimitExceededException::new);

        // 날짜 변경 시뮬레이션 (리플렉션 사용)
        try {
            java.lang.reflect.Field dateField = DailyLimitTracker.class.getDeclaredField("lastTransactionDate");
            dateField.setAccessible(true);
            dateField.set(tracker, LocalDate.now().minusDays(1));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // when
        tracker.trackAndCheckLimit(3_000_000L, DailyTransferLimitExceededException::new);

        // then
        assertThat(tracker.getAccumulatedAmount()).isEqualTo(3_000_000L);
    }

    @Test
    @DisplayName("0원 이하로 추적하면 예외가 발생한다")
    void track_zero_or_negative_amount_throws_exception() {
        // given
        WithdrawLimitTracker tracker = WithdrawLimitTracker.create();

        // when & then
        assertThatThrownBy(() -> tracker.trackAndCheckLimit(0L, DailyWithdrawLimitExceededException::new))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다.");

        assertThatThrownBy(() -> tracker.trackAndCheckLimit(-1000L, DailyWithdrawLimitExceededException::new))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("금액은 0보다 커야 합니다.");
    }
}
