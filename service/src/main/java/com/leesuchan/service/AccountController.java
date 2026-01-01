package com.leesuchan.service;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.DeleteAccountUseCase;
import com.leesuchan.account.service.DepositMoneyUseCase;
import com.leesuchan.account.service.RegisterAccountUseCase;
import com.leesuchan.account.service.TransferMoneyUseCase;
import com.leesuchan.account.service.WithdrawMoneyUseCase;
import com.leesuchan.service.GetAccountQueryService;
import com.leesuchan.service.GetAccountsQueryService;
import com.leesuchan.common.response.ApiResponse;
import com.leesuchan.service.dto.AccountResponse;
import com.leesuchan.account.service.TransferMoneyUseCase.TransferResult;
import com.leesuchan.service.dto.ActivityResponse;
import com.leesuchan.service.dto.DepositDto;
import com.leesuchan.service.dto.RegisterAccountDto;
import com.leesuchan.service.dto.TransferDto;
import com.leesuchan.service.dto.TransferResponse;
import com.leesuchan.service.dto.WithdrawDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 계좌 API Controller
 */
@Tag(name = "계좌 관리", description = "계좌 관련 API")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final RegisterAccountUseCase registerAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;
    private final GetActivitiesQueryService getActivitiesQueryService;
    private final GetAccountQueryService getAccountQueryService;
    private final GetAccountsQueryService getAccountsQueryService;

    /**
     * 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
     */
    public AccountController(
            RegisterAccountUseCase registerAccountUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            DepositMoneyUseCase depositMoneyUseCase,
            WithdrawMoneyUseCase withdrawMoneyUseCase,
            TransferMoneyUseCase transferMoneyUseCase,
            GetActivitiesQueryService getActivitiesQueryService,
            GetAccountQueryService getAccountQueryService,
            GetAccountsQueryService getAccountsQueryService
    ) {
        this.registerAccountUseCase = registerAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.transferMoneyUseCase = transferMoneyUseCase;
        this.getActivitiesQueryService = getActivitiesQueryService;
        this.getAccountQueryService = getAccountQueryService;
        this.getAccountsQueryService = getAccountsQueryService;
    }

    /**
     * 계좌 목록 조회
     */
    @Operation(
            summary = "계좌 목록 조회",
            description = "계좌 목록을 페이지네이션으로 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "계좌 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public ApiResponse<Page<AccountResponse>> getAccounts(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AccountResponse> accounts = getAccountsQueryService.execute(pageable);
        return ApiResponse.success(accounts);
    }

    /**
     * 계좌 등록
     */
    @Operation(
            summary = "계좌 등록",
            description = "새로운 계좌를 등록합니다. 초기 잔액은 0원으로 시작합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "계좌 등록 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 파라미터 유효성 검증 실패"
            )
    })
    @PostMapping
    public ApiResponse<AccountResponse> register(
            @Parameter(description = "계좌 등록 요청", required = true)
            @Valid @RequestBody RegisterAccountDto request
    ) {
        Account account = registerAccountUseCase.execute(request.accountNumber(), request.accountName());
        return ApiResponse.success(AccountResponse.from(account));
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

    /**
     * 계좌 조회
     */
    @Operation(
            summary = "계좌 단건 조회",
            description = "계좌번호로 계좌 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "계좌 조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "계좌를 찾을 수 없습니다"
            )
    })
    @GetMapping("/{accountNumber}")
    public ApiResponse<AccountResponse> getAccount(
            @Parameter(description = "계좌번호", example = "1234567890", required = true)
            @PathVariable String accountNumber
    ) {
        AccountResponse response = getAccountQueryService.execute(accountNumber);
        return ApiResponse.success(response);
    }

    /**
     * 계좌 삭제
     */
    @Operation(
            summary = "계좌 삭제",
            description = "계좌를 삭제합니다. 소프트 삭제로 처리됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "계좌 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "계좌를 찾을 수 없습니다"
            )
    })
    @DeleteMapping("/{accountNumber}")
    public ApiResponse<Void> deleteAccount(
            @Parameter(description = "계좌번호", example = "1234567890", required = true)
            @PathVariable String accountNumber
    ) {
        deleteAccountUseCase.execute(accountNumber);
        return ApiResponse.success(null);
    }

    /**
     * 거래내역 조회
     */
    @Operation(
            summary = "거래내역 조회",
            description = "계좌의 거래내역을 조회합니다. 최신순으로 정렬됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "거래내역 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "계좌를 찾을 수 없습니다"
            )
    })
    @GetMapping("/{accountNumber}/activities")
    public ApiResponse<List<ActivityResponse>> getActivities(
            @Parameter(description = "계좌번호", example = "1234567890", required = true)
            @PathVariable String accountNumber
    ) {
        List<ActivityResponse> activities = getActivitiesQueryService.execute(accountNumber);
        return ApiResponse.success(activities);
    }
}
