package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.DeleteAccountUseCase;
import com.leesuchan.account.service.DepositMoneyUseCase;
import com.leesuchan.account.service.RegisterAccountUseCase;
import com.leesuchan.account.service.TransferMoneyUseCase;
import com.leesuchan.account.service.WithdrawMoneyUseCase;
import com.leesuchan.common.response.ApiResponse;
import com.leesuchan.service.dto.AccountResponse;
import com.leesuchan.account.service.TransferMoneyUseCase.TransferResult;
import com.leesuchan.service.dto.DepositDto;
import com.leesuchan.service.dto.RegisterAccountDto;
import com.leesuchan.service.dto.TransferDto;
import com.leesuchan.service.dto.TransferResponse;
import com.leesuchan.service.dto.WithdrawDto;
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
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;

    /**
     * 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
     */
    public AccountController(
            RegisterAccountUseCase registerAccountUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            DepositMoneyUseCase depositMoneyUseCase,
            WithdrawMoneyUseCase withdrawMoneyUseCase,
            TransferMoneyUseCase transferMoneyUseCase
    ) {
        this.registerAccountUseCase = registerAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.transferMoneyUseCase = transferMoneyUseCase;
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
     * 출금
     */
    @PostMapping("/withdraw")
    public ApiResponse<AccountResponse> withdraw(@Valid @RequestBody WithdrawDto request) {
        Account account = withdrawMoneyUseCase.execute(request.accountNumber(), request.amount());
        return ApiResponse.success(AccountResponse.from(account));
    }

    /**
     * 이체
     */
    @PostMapping("/transfer")
    public ApiResponse<TransferResponse> transfer(@Valid @RequestBody TransferDto request) {
        TransferResult result = transferMoneyUseCase.execute(
                request.fromAccountNumber(),
                request.toAccountNumber(),
                request.amount()
        );
        return ApiResponse.success(new TransferResponse(
                AccountResponse.from(result.from()),
                AccountResponse.from(result.to()),
                result.fee()
        ));
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
