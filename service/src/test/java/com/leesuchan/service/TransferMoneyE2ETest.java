package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.*;
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
@DisplayName("이체 E2E 테스트")
class TransferMoneyE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferMoneyUseCase transferMoneyUseCase;

    @MockBean
    private RegisterAccountUseCase registerAccountUseCase;

    @MockBean
    private DeleteAccountUseCase deleteAccountUseCase;

    @MockBean
    private DepositMoneyUseCase depositMoneyUseCase;

    @MockBean
    private WithdrawMoneyUseCase withdrawMoneyUseCase;

    @BeforeEach
    void setUp() {
        reset(transferMoneyUseCase);
    }

    @Test
    @DisplayName("이체 API를 호출한다")
    void transfer_money_api() throws Exception {
        // given
        String requestBody = """
                {
                    "fromAccountNumber": "1234567890",
                    "toAccountNumber": "0987654321",
                    "amount": 10000
                }
                """;

        Account from = Account.create("1234567890", "출금 계좌");
        from.deposit(50000L);
        Account to = Account.create("0987654321", "입금 계좌");

        from.transfer(to, 10000L);

        when(transferMoneyUseCase.execute(eq("1234567890"), eq("0987654321"), eq(10000L))).thenReturn(
                new TransferMoneyUseCase.TransferResult(from, to, 100L)
        );

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions/transfer")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.data.fromAccount.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.data.toAccount.accountNumber").value("0987654321"))
                .andExpect(jsonPath("$.data.fee").value(100));

        verify(transferMoneyUseCase).execute(eq("1234567890"), eq("0987654321"), eq(10000L));
    }

    @Test
    @DisplayName("출금 계좌번호가 비어있으면 400 에러가 발생한다")
    void transfer_empty_from_account_number_400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions/transfer")
                        .contentType("application/json")
                        .content("""
                                {
                                    "fromAccountNumber": "",
                                    "toAccountNumber": "0987654321",
                                    "amount": 10000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.success").value(false));

        verify(transferMoneyUseCase, never()).execute(any(), any(), any());
    }

    @Test
    @DisplayName("입금 계좌번호가 비어있으면 400 에러가 발생한다")
    void transfer_empty_to_account_number_400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions/transfer")
                        .contentType("application/json")
                        .content("""
                                {
                                    "fromAccountNumber": "1234567890",
                                    "toAccountNumber": "",
                                    "amount": 10000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.success").value(false));

        verify(transferMoneyUseCase, never()).execute(any(), any(), any());
    }

    @Test
    @DisplayName("금액이 0 이하면 400 에러가 발생한다")
    void transfer_zero_or_negative_amount_400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions/transfer")
                        .contentType("application/json")
                        .content("""
                                {
                                    "fromAccountNumber": "1234567890",
                                    "toAccountNumber": "0987654321",
                                    "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status.success").value(false));

        verify(transferMoneyUseCase, never()).execute(any(), any(), any());
    }
}
