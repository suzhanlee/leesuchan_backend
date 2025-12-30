package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.RegisterAccountUseCase;
import com.leesuchan.common.response.ApiResponse;
import com.leesuchan.service.dto.AccountResponse;
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

    /**
     * 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
     */
    public AccountController(RegisterAccountUseCase registerAccountUseCase) {
        this.registerAccountUseCase = registerAccountUseCase;
    }

    /**
     * 계좌 등록
     */
    @PostMapping
    public ApiResponse<AccountResponse> register(@Valid @RequestBody RegisterAccountDto request) {
        Account account = registerAccountUseCase.register(request.accountNumber(), request.accountName());
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
}
