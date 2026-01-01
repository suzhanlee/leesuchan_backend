package com.leesuchan.service.controller;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.DepositMoneyUseCase;
import com.leesuchan.account.service.TransferMoneyUseCase;
import com.leesuchan.account.service.WithdrawMoneyUseCase;
import com.leesuchan.common.response.ApiResponse;
import com.leesuchan.service.dto.request.DepositDto;
import com.leesuchan.service.dto.request.TransferDto;
import com.leesuchan.service.dto.request.WithdrawDto;
import com.leesuchan.service.dto.response.AccountResponse;
import com.leesuchan.service.dto.response.TransferResponse;
import com.leesuchan.account.service.TransferMoneyUseCase.TransferResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 거래(입금/출금/이체) API Controller
 */
@Tag(name = "거래 실행", description = "입금/출금/이체 API")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;

    public TransactionController(
            DepositMoneyUseCase depositMoneyUseCase,
            WithdrawMoneyUseCase withdrawMoneyUseCase,
            TransferMoneyUseCase transferMoneyUseCase
    ) {
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.transferMoneyUseCase = transferMoneyUseCase;
    }

    /**
     * 입금
     */
    @Operation(
            summary = "입금",
            description = "계좌에 금액을 입금합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "입금 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 파라미터 유효성 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_001",
                    description = "계좌를 찾을 수 없습니다"
            )
    })
    @PostMapping("/deposit")
    public ApiResponse<AccountResponse> deposit(
            @Parameter(description = "입금 요청", required = true)
            @Valid @RequestBody DepositDto request
    ) {
        Account account = depositMoneyUseCase.execute(request.accountNumber(), request.amount());
        return ApiResponse.success(AccountResponse.from(account));
    }

    /**
     * 출금
     */
    @Operation(
            summary = "출금",
            description = "계좌에서 금액을 출금합니다. 일일 출금 한도는 100만 원입니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "출금 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 파라미터 유효성 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_001",
                    description = "계좌를 찾을 수 없습니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_004",
                    description = "잔액이 부족합니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_005",
                    description = "일일 출금 한도를 초과했습니다 (1,000,000원)"
            )
    })
    @PostMapping("/withdraw")
    public ApiResponse<AccountResponse> withdraw(
            @Parameter(description = "출금 요청", required = true)
            @Valid @RequestBody WithdrawDto request
    ) {
        Account account = withdrawMoneyUseCase.execute(request.accountNumber(), request.amount());
        return ApiResponse.success(AccountResponse.from(account));
    }

    /**
     * 이체
     */
    @Operation(
            summary = "이체",
            description = "한 계좌에서 다른 계좌로 금액을 이체합니다. 일일 이체 한도는 300만 원이며, 수수료는 이체 금액의 1%입니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이체 성공",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 파라미터 유효성 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_001",
                    description = "계좌를 찾을 수 없습니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_004",
                    description = "잔액이 부족합니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_006",
                    description = "일일 이체 한도를 초과했습니다 (3,000,000원)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "ACCOUNT_007",
                    description = "동일 계좌로 이체할 수 없습니다"
            )
    })
    @PostMapping("/transfer")
    public ApiResponse<TransferResponse> transfer(
            @Parameter(description = "이체 요청", required = true)
            @Valid @RequestBody TransferDto request
    ) {
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
}
