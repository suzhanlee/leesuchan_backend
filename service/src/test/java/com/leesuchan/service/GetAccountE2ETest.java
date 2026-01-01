package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.service.application.GetAccountQueryService;
import com.leesuchan.service.dto.response.AccountResponse;
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
@DisplayName("계좌 조회 E2E 테스트")
class GetAccountE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAccountQueryService getAccountQueryService;

    @BeforeEach
    void setUp() {
        reset(getAccountQueryService);
    }

    @Test
    @DisplayName("계좌를 조회하는 API를 호출한다")
    void get_account_api() throws Exception {
        // given
        String accountNumber = "1234567890";
        Account mockAccount = Account.create(accountNumber, "테스트 계좌");
        mockAccount.deposit(50000L);

        when(getAccountQueryService.execute(accountNumber)).thenReturn(AccountResponse.from(mockAccount));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.data.accountName").value("테스트 계좌"))
                .andExpect(jsonPath("$.data.balance").value(50000L));

        verify(getAccountQueryService).execute(accountNumber);
    }

    @Test
    @DisplayName("계좌가 없으면 404 에러가 발생한다")
    void account_not_found_404() throws Exception {
        // given
        String accountNumber = "9999999999";
        when(getAccountQueryService.execute(accountNumber)).thenThrow(new com.leesuchan.account.domain.exception.AccountNotFoundException());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.success").value(false));

        verify(getAccountQueryService).execute(accountNumber);
    }
}
