package com.leesuchan.service;

import com.leesuchan.account.service.DeleteAccountUseCase;
import com.leesuchan.account.service.RegisterAccountUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("계좌 삭제 E2E 테스트")
class DeleteAccountE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeleteAccountUseCase deleteAccountUseCase;

    @MockBean
    private RegisterAccountUseCase registerAccountUseCase;

    @BeforeEach
    void setUp() {
        reset(deleteAccountUseCase);
    }

    @Test
    @DisplayName("계좌를 삭제하는 API를 호출한다")
    void delete_account_api() throws Exception {
        // given
        String accountNumber = "1234567890";
        doNothing().when(deleteAccountUseCase).execute(any());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/accounts/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.success").value(true))
                .andExpect(jsonPath("$.status.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(deleteAccountUseCase).execute(eq(accountNumber));
    }
}
