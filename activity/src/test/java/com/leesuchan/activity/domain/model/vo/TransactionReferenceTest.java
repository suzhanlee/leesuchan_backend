package com.leesuchan.activity.domain.model.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TransactionReference 테스트")
class TransactionReferenceTest {

    @Test
    @DisplayName("참조 정보를 생성한다")
    void create_reference() {
        // given
        Long accountId = 1L;
        String accountNumber = "1234567890";

        // when
        TransactionReference reference = TransactionReference.of(accountId, accountNumber);

        // then
        assertThat(reference.getAccountId()).isEqualTo(accountId);
        assertThat(reference.getAccountNumber()).isEqualTo(accountNumber);
    }

    @Test
    @DisplayName("null 계좌 ID로 생성하면 예외가 발생한다")
    void create_with_null_account_id_throws_exception() {
        // when & then
        assertThatThrownBy(() -> TransactionReference.of(null, "1234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("참조 계좌 ID는 필수입니다.");
    }

    @Test
    @DisplayName("빈 계좌번호로 생성하면 예외가 발생한다")
    void create_with_empty_account_number_throws_exception() {
        // when & then
        assertThatThrownBy(() -> TransactionReference.of(1L, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("참조 계좌번호는 필수입니다.");

        assertThatThrownBy(() -> TransactionReference.of(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("참조 계좌번호는 필수입니다.");
    }

    @Test
    @DisplayName("같은 값으로 생성하면 동등하다")
    void equals_same_values() {
        // given
        TransactionReference ref1 = TransactionReference.of(1L, "1234567890");
        TransactionReference ref2 = TransactionReference.of(1L, "1234567890");

        // then
        assertThat(ref1).isEqualTo(ref2);
        assertThat(ref1.hashCode()).isEqualTo(ref2.hashCode());
    }

    @Test
    @DisplayName("다른 값으로 생성하면 동등하지 않다")
    void equals_different_values() {
        // given
        TransactionReference ref1 = TransactionReference.of(1L, "1234567890");
        TransactionReference ref2 = TransactionReference.of(2L, "0987654321");

        // then
        assertThat(ref1).isNotEqualTo(ref2);
    }

    @Test
    @DisplayName("계좌 ID가 다르면 동등하지 않다")
    void equals_different_account_id() {
        // given
        TransactionReference ref1 = TransactionReference.of(1L, "1234567890");
        TransactionReference ref2 = TransactionReference.of(2L, "1234567890");

        // then
        assertThat(ref1).isNotEqualTo(ref2);
    }

    @Test
    @DisplayName("계좌번호가 다르면 동등하지 않다")
    void equals_different_account_number() {
        // given
        TransactionReference ref1 = TransactionReference.of(1L, "1234567890");
        TransactionReference ref2 = TransactionReference.of(1L, "0987654321");

        // then
        assertThat(ref1).isNotEqualTo(ref2);
    }
}
