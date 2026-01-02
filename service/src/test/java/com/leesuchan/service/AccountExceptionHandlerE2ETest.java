package com.leesuchan.service;

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

/**
 * AccountExceptionHandler HTTP 상태 코드 검증 E2E 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AccountExceptionHandler E2E 테스트")
class AccountExceptionHandlerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAccountQueryService getAccountQueryService;

    @BeforeEach
    void setUp() {
        reset(getAccountQueryService);
    }

    @Test
    @DisplayName("AccountNotFoundException 발생 시 404 NOT_FOUND를 반환한다")
    void account_not_found_returns_404() throws Exception {
        // given
        String accountNumber = "9999999999";
        when(getAccountQueryService.execute(accountNumber))
                .thenThrow(new com.leesuchan.account.domain.exception.AccountNotFoundException());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status.success").value(false))
                .andExpect(jsonPath("$.status.code").value("ACCOUNT_001"));

        verify(getAccountQueryService).execute(accountNumber);
    }

    @Test
    @DisplayName("정상 조회 시 200 OK를 반환한다")
    void successful_query_returns_200() throws Exception {
        // given
        String accountNumber = "1234567890";
        AccountResponse response = new AccountResponse(
                1L,
                accountNumber,
                "테스트 계좌",
                10000L,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now()
        );

        when(getAccountQueryService.execute(accountNumber)).thenReturn(response);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true));

        verify(getAccountQueryService).execute(accountNumber);
    }
}
