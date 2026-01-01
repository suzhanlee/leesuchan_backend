package com.leesuchan.activity.domain.model;

import com.leesuchan.activity.domain.model.vo.TransactionReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Activity 엔티티 테스트")
class ActivityTest {

    @Test
    @DisplayName("입금 Activity를 생성한다")
    void create_deposit_activity() {
        // given
        Long accountId = 1L;
        Long amount = 10000L;
        Long balanceAfter = 10000L;

        // when
        Activity activity = Activity.deposit(accountId, amount, balanceAfter);

        // then
        assertThat(activity.getAccountId()).isEqualTo(accountId);
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.DEPOSIT);
        assertThat(activity.getAmount()).isEqualTo(amount);
        assertThat(activity.getBalanceAfter()).isEqualTo(balanceAfter);
        assertThat(activity.getFee()).isZero();
        assertThat(activity.getTransactionReference()).isNull();
        assertThat(activity.getDescription()).isNull();
        assertThat(activity.getTransactionId()).isNull();
        assertThat(activity.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("출금 Activity를 생성한다")
    void create_withdraw_activity() {
        // given
        Long accountId = 1L;
        Long amount = 5000L;
        Long balanceAfter = 5000L;

        // when
        Activity activity = Activity.withdraw(accountId, amount, balanceAfter);

        // then
        assertThat(activity.getAccountId()).isEqualTo(accountId);
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.WITHDRAW);
        assertThat(activity.getAmount()).isEqualTo(amount);
        assertThat(activity.getBalanceAfter()).isEqualTo(balanceAfter);
        assertThat(activity.getFee()).isZero();
        assertThat(activity.getTransactionReference()).isNull();
    }

    @Test
    @DisplayName("이체 출금 Activity를 생성한다")
    void create_transfer_out_activity() {
        // given
        Long accountId = 1L;
        Long referenceAccountId = 2L;
        String referenceAccountNumber = "0987654321";
        Long amount = 10000L;
        Long fee = 100L;
        Long balanceAfter = 39900L;
        String transactionId = "tx-12345";

        // when
        Activity activity = Activity.transferOut(
                accountId,
                referenceAccountId,
                referenceAccountNumber,
                amount,
                fee,
                balanceAfter,
                transactionId
        );

        // then
        assertThat(activity.getAccountId()).isEqualTo(accountId);
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.TRANSFER_OUT);
        assertThat(activity.getAmount()).isEqualTo(amount);
        assertThat(activity.getFee()).isEqualTo(fee);
        assertThat(activity.getBalanceAfter()).isEqualTo(balanceAfter);
        assertThat(activity.getTransactionReference()).isNotNull();
        assertThat(activity.getTransactionReference().getAccountId()).isEqualTo(referenceAccountId);
        assertThat(activity.getTransactionReference().getAccountNumber()).isEqualTo(referenceAccountNumber);
        assertThat(activity.getTransactionId()).isEqualTo(transactionId);
        assertThat(activity.getDescription()).isNull();
    }

    @Test
    @DisplayName("이체 입금 Activity를 생성한다")
    void create_transfer_in_activity() {
        // given
        Long accountId = 2L;
        Long referenceAccountId = 1L;
        String referenceAccountNumber = "1234567890";
        Long amount = 10000L;
        Long balanceAfter = 20000L;
        String transactionId = "tx-12345";

        // when
        Activity activity = Activity.transferIn(
                accountId,
                referenceAccountId,
                referenceAccountNumber,
                amount,
                balanceAfter,
                transactionId
        );

        // then
        assertThat(activity.getAccountId()).isEqualTo(accountId);
        assertThat(activity.getActivityType()).isEqualTo(ActivityType.TRANSFER_IN);
        assertThat(activity.getAmount()).isEqualTo(amount);
        assertThat(activity.getFee()).isZero(); // 입금자는 수수료 없음
        assertThat(activity.getBalanceAfter()).isEqualTo(balanceAfter);
        assertThat(activity.getTransactionReference()).isNotNull();
        assertThat(activity.getTransactionReference().getAccountId()).isEqualTo(referenceAccountId);
        assertThat(activity.getTransactionReference().getAccountNumber()).isEqualTo(referenceAccountNumber);
        assertThat(activity.getTransactionId()).isEqualTo(transactionId);
        assertThat(activity.getDescription()).isNull();
    }

    @Test
    @DisplayName("입금 Activity는 참조 정보가 없다")
    void deposit_activity_has_no_reference() {
        // given & when
        Activity activity = Activity.deposit(1L, 10000L, 10000L);

        // then
        assertThat(activity.getTransactionReference()).isNull();
    }

    @Test
    @DisplayName("출금 Activity는 참조 정보가 없다")
    void withdraw_activity_has_no_reference() {
        // given & when
        Activity activity = Activity.withdraw(1L, 5000L, 5000L);

        // then
        assertThat(activity.getTransactionReference()).isNull();
    }

    @Test
    @DisplayName("이체 Activity는 참조 정보가 있다")
    void transfer_activity_has_reference() {
        // given & when
        Activity activity = Activity.transferOut(1L, 2L, "0987654321", 10000L, 100L, 39900L, "tx-12345");

        // then
        assertThat(activity.getTransactionReference()).isNotNull();
        assertThat(activity.getTransactionReference().getAccountId()).isEqualTo(2L);
        assertThat(activity.getTransactionReference().getAccountNumber()).isEqualTo("0987654321");
    }

    @Test
    @DisplayName("id가 같으면 같은 Activity로 간주한다")
    void equals_same_id() {
        // given
        Activity activity1 = Activity.deposit(1L, 10000L, 10000L);
        Activity activity2 = Activity.deposit(1L, 10000L, 10000L);

        // reflectively set same id
        try {
            java.lang.reflect.Field idField = Activity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(activity1, 1L);
            idField.set(activity2, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // then
        assertThat(activity1).isEqualTo(activity2);
        assertThat(activity1.hashCode()).isEqualTo(activity2.hashCode());
    }

    @Test
    @DisplayName("id가 다르면 다른 Activity로 간주한다")
    void equals_different_id() {
        // given
        Activity activity1 = Activity.deposit(1L, 10000L, 10000L);
        Activity activity2 = Activity.deposit(1L, 10000L, 10000L);

        // reflectively set different id
        try {
            java.lang.reflect.Field idField = Activity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(activity1, 1L);
            idField.set(activity2, 2L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // then
        assertThat(activity1).isNotEqualTo(activity2);
        assertThat(activity1.hashCode()).isNotEqualTo(activity2.hashCode());
    }
}
