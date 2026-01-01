package com.leesuchan.service.controller;

import com.leesuchan.account.domain.model.Account;
import com.leesuchan.account.service.DeleteAccountUseCase;
import com.leesuchan.account.service.RegisterAccountUseCase;
import com.leesuchan.service.application.GetAccountQueryService;
import com.leesuchan.service.application.GetAccountsQueryService;
import com.leesuchan.common.response.ApiResponse;
import com.leesuchan.service.dto.request.RegisterAccountDto;
import com.leesuchan.service.dto.response.AccountResponse;
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

/**
 * 계좌 API Controller
 */
@Tag(name = "계좌 관리", description = "계좌 CRUD API")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final RegisterAccountUseCase registerAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final GetAccountQueryService getAccountQueryService;
    private final GetAccountsQueryService getAccountsQueryService;

    /**
     * 생성자 주입 (생성자가 하나인 경우 @Autowired 생략 가능)
     */
    public AccountController(
            RegisterAccountUseCase registerAccountUseCase,
            DeleteAccountUseCase deleteAccountUseCase,
            GetAccountQueryService getAccountQueryService,
            GetAccountsQueryService getAccountsQueryService
    ) {
        this.registerAccountUseCase = registerAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
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
            @Parameter(description = "페이지 정보", example = "{\"page\": 0, \"size\": 20}")
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
     * 계좌 단건 조회
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
}
