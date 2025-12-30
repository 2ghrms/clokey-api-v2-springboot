package org.clokey.domain.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.enums.Season;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.statistics.dto.response.ClosetUtilizationResponse;
import org.clokey.domain.statistics.dto.response.FavoriteCategoryItemsResponse;
import org.clokey.domain.statistics.dto.response.FavoriteItemsResponse;
import org.clokey.domain.statistics.dto.response.StatisticsCheckConditionResponse;
import org.clokey.domain.statistics.service.StatisticsService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * TODO: 앞으로, 운영이 활성화 되고 트래픽이 많아지게 되면 해당 API의 성능에 대한 점검이 필요합니다. 통계 테이블을 따로 분리하거나, Redis를 통한 캐싱을 고려해
 * 보세요.
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Tag(name = "16. 통계 API", description = "통계 관련 API입니다.")
@Validated
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/check-conditions")
    @Operation(
            operationId = "Statistics_checkStatisticsCondition",
            summary = "통계 최소 조건 확인",
            description = "통계 집계가 가능한 최소 조건을 확인하는 API입니다.")
    public BaseResponse<StatisticsCheckConditionResponse> checkStatisticsCondition() {
        StatisticsCheckConditionResponse response = statisticsService.checkStatisticsCondition();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/favorite-category-items")
    @Operation(
            operationId = "Statistics_getFavoriteCategoryItems",
            summary = "카테고리별 최애 아이템 조회",
            description = "카테고리별 아이템의 개수와 점유율을 조회하는 API입니다..")
    public BaseResponse<FavoriteCategoryItemsResponse> getFavoriteCategoryItems(
            @Parameter(description = "카테고리 ID") @RequestParam Long categoryId) {
        FavoriteCategoryItemsResponse response =
                statisticsService.getFavoriteCategoryItems(categoryId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/favorite-items")
    @Operation(
            operationId = "Statistics_getFavoriteItems",
            summary = "옷장 아이템 통계 조회",
            description = "옷장 아이템 통계를 조회합니다.")
    public BaseResponse<FavoriteItemsResponse> getFavoriteItems() {
        FavoriteItemsResponse response = statisticsService.getFavoriteItems();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/closet-utilization")
    @Operation(
            operationId = "Statistics_getClosetUtilization",
            summary = "옷장 활용도 조회",
            description =
                    "시즌별 옷장 활용도를 조회합니다. HistoryClothTag에 태그되었거나 Daily Coordinate에 포함된 옷을 활용된 것으로 간주합니다.")
    public BaseResponse<ClosetUtilizationResponse> getClosetUtilization(
            @Parameter(description = "시즌", example = "SPRING") @RequestParam Season season) {
        ClosetUtilizationResponse response = statisticsService.getClosetUtilization(season);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
}
