package com.leesuchan.service.controller;

import com.leesuchan.service.application.GetActivitiesQueryService;
import com.leesuchan.common.response.ApiResponse;
import com.leesuchan.service.dto.response.ActivityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 거래내역 API Controller
 */
@Tag(name = "거래내역 관리", description = "거래내역 조회 API")
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final GetActivitiesQueryService getActivitiesQueryService;

    public ActivityController(GetActivitiesQueryService getActivitiesQueryService) {
        this.getActivitiesQueryService = getActivitiesQueryService;
    }

    /**
     * 계좌 거래내역 조회
     */
    @Operation(
            summary = "계좌 거래내역 조회",
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
    @GetMapping("/{accountNumber}")
    public ApiResponse<List<ActivityResponse>> getActivities(
            @Parameter(description = "계좌번호", example = "1234567890", required = true)
            @PathVariable String accountNumber
    ) {
        List<ActivityResponse> activities = getActivitiesQueryService.execute(accountNumber);
        return ApiResponse.success(activities);
    }
}
