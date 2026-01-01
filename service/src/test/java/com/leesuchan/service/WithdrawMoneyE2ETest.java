package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.DeleteAccountUseCase;
import com.leesuchan.account.service.DepositMoneyUseCase;
import com.leesuchan.account.service.RegisterAccountUseCase;
import com.leesuchan.account.service.WithdrawMoneyUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("출금 E2E 테스트")
class WithdrawMoneyE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WithdrawMoneyUseCase withdrawMoneyUseCase;

    @MockBean
    private RegisterAccountUseCase registerAccountUseCase;

    @MockBean
    private DeleteAccountUseCase deleteAccountUseCase;

    @MockBean
    private DepositMoneyUseCase depositMoneyUseCase;

    @BeforeEach
    void setUp() {
        reset(withdrawMoneyUseCase);
    }

    @Test
    @DisplayName("출금 API를 호출한다")
    void withdraw_money_api() throws Exception {
        // given
        String requestBody = """
                {
                    "accountNumber": "1234567890",
                    "amount": 5000
                }
                """;

        Account mockAccount = Account.create("1234567890", "테스트 계좌");
        mockAccount.deposit(10000L);
        mockAccount.withdraw(5000L);

        when(withdrawMoneyUseCase.execute("1234567890", 5000L)).thenReturn(mockAccount);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/withdraw")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.data.balance").value(5000));

        verify(withdrawMoneyUseCase).execute("1234567890", 5000L);
    }

    @Test
    @DisplayName("계좌번호가 비어있으면 400 에러가 발생한다")
    void withdraw_empty_account_number_400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/withdraw")
                        .contentType("application/json")
                        .content("""
                                {
                                    "accountNumber": "",
                                    "amount": 5000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.success").value(false));

        verify(withdrawMoneyUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("금액이 0 이하면 400 에러가 발생한다")
    void withdraw_zero_or_negative_amount_400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts/withdraw")
                        .contentType("application/json")
                        .content("""
                                {
                                    "accountNumber": "1234567890",
                                    "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.success").value(false));

        verify(withdrawMoneyUseCase, never()).execute(any(), any());
    }
}
