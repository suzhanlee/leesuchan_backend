package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.RegisterAccountUseCase;
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
@DisplayName("계좌 등록 E2E 테스트")
class RegisterAccountE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterAccountUseCase registerAccountUseCase;

    @BeforeEach
    void setUp() {
        reset(registerAccountUseCase);
    }

    @Test
    @DisplayName("계좌를 등록하는 API를 호출한다")
    void register_account_api() throws Exception {
        // given
        String requestBody = """
                {
                    "accountNumber": "1234567890",
                    "accountName": "테스트 계좌"
                }
                """;

        Account mockAccount = Account.create("1234567890", "테스트 계좌");
        when(registerAccountUseCase.execute("1234567890", "테스트 계좌")).thenReturn(mockAccount);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/accounts")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.data.accountName").value("테스트 계좌"))
                .andExpect(jsonPath("$.data.balance").value(0));
    }
}
