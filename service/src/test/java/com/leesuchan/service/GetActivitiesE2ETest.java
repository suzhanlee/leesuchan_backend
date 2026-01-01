package com.leesuchan.service;

import com.leesuchan.account.service.DeleteAccountUseCase;
import com.leesuchan.account.service.DepositMoneyUseCase;
import com.leesuchan.account.service.RegisterAccountUseCase;
import com.leesuchan.account.service.TransferMoneyUseCase;
import com.leesuchan.account.service.WithdrawMoneyUseCase;
import com.leesuchan.activity.domain.model.Activity;
import com.leesuchan.activity.domain.model.ActivityType;
import com.leesuchan.service.dto.ActivityResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("거래내역 조회 E2E 테스트")
class GetActivitiesE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetActivitiesQueryService getActivitiesQueryService;

    @MockBean
    private RegisterAccountUseCase registerAccountUseCase;

    @MockBean
    private DeleteAccountUseCase deleteAccountUseCase;

    @MockBean
    private DepositMoneyUseCase depositMoneyUseCase;

    @MockBean
    private WithdrawMoneyUseCase withdrawMoneyUseCase;

    @MockBean
    private TransferMoneyUseCase transferMoneyUseCase;

    @BeforeEach
    void setUp() {
        reset(getActivitiesQueryService);
    }

    @Test
    @DisplayName("거래내역 조회 API를 호출한다")
    void get_activities_api() throws Exception {
        // given
        String accountNumber = "1234567890";

        List<ActivityResponse> activities = List.of(
                new ActivityResponse(
                        1L,
                        ActivityType.DEPOSIT,
                        10000L,
                        0L,
                        10000L,
                        null,
                        null,
                        LocalDateTime.now()
                ),
                new ActivityResponse(
                        2L,
                        ActivityType.WITHDRAW,
                        5000L,
                        0L,
                        5000L,
                        null,
                        null,
                        LocalDateTime.now()
                )
        );

        when(getActivitiesQueryService.execute(eq(accountNumber))).thenReturn(activities);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/{accountNumber}/activities", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].activityType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data[0].amount").value(10000))
                .andExpect(jsonPath("$.data[1].activityType").value("WITHDRAW"))
                .andExpect(jsonPath("$.data[1].amount").value(5000));

        verify(getActivitiesQueryService).execute(eq(accountNumber));
    }

    @Test
    @DisplayName("거래내역이 없으면 빈 목록을 반환한다")
    void get_empty_activities_api() throws Exception {
        // given
        String accountNumber = "1234567890";

        when(getActivitiesQueryService.execute(eq(accountNumber))).thenReturn(List.of());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/accounts/{accountNumber}/activities", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(getActivitiesQueryService).execute(eq(accountNumber));
    }
}
