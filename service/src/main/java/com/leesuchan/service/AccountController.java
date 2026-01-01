package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.DeleteAccountUseCase;
import com.leesuchan.account.service.DepositMoneyUseCase;
import com.leesuchan.account.service.RegisterAccountUseCase;
import com.leesuchan.common.response.ApiResponse;
import com.leesuchan.service.dto.AccountResponse;
import com.leesuchan.service.dto.DepositDto;
import com.leesuchan.service.dto.RegisterAccountDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 계좌 API Controller
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final RegisterAccountUseCase registerAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;

    /**
     * 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
     */
    public AccountController(
            RegisterAccountUseCase registerAccountUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            DepositMoneyUseCase depositMoneyUseCase
    ) {
        this.registerAccountUseCase = registerAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
    }

    /**
     * 계좌 등록
     */
    @PostMapping
    public ApiResponse<AccountResponse> register(@Valid @RequestBody RegisterAccountDto request) {
        Account account = registerAccountUseCase.execute(request.accountNumber(), request.accountName());
        return ApiResponse.success(AccountResponse.from(account));
    }

    /**
     * 입금
     */
    @PostMapping("/deposit")
    public ApiResponse<AccountResponse> deposit(@Valid @RequestBody DepositDto request) {
        Account account = depositMoneyUseCase.execute(request.accountNumber(), request.amount());
        return ApiResponse.success(AccountResponse.from(account));
    }

    /**
     * 계좌 조회
     */
    @GetMapping("/{accountNumber}")
    public ApiResponse<AccountResponse> getAccount(@PathVariable String accountNumber) {
        // TODO: 구현 필요
        return ApiResponse.success(null);
    }

    /**
     * 계좌 삭제
     */
    @DeleteMapping("/{accountNumber}")
    public ApiResponse<Void> deleteAccount(@PathVariable String accountNumber) {
        deleteAccountUseCase.execute(accountNumber);
        return ApiResponse.success(null);
    }
}
