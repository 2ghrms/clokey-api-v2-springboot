package org.clokey.domain.cloth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.enums.Season;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.request.ClothUpdateRequest;
import org.clokey.domain.cloth.dto.response.*;
import org.clokey.domain.cloth.service.ClothService;
import org.clokey.global.annotation.PageSize;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clothes")
@RequiredArgsConstructor
@Tag(name = "03. 옷 API", description = "옷 관련 API입니다.")
@Validated
public class ClothController {

    private final ClothService clothService;

    @PostMapping
    @Operation(operationId = "Cloth_createClothes", summary = "옷 생성", description = "새로운 옷을 생성합니다.")
    public BaseResponse<ClothCreateResponse> createClothes(
            @Valid @RequestBody ClothCreateRequests request) {
        ClothCreateResponse response = clothService.createClothes(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @GetMapping("/recommend")
    @Operation(
            operationId = "Cloth_recommendCategoryClothes",
            summary = "카테고리별 계절에 맞는 옷 조회",
            description = "카테고리별로 계절에 맞는 옷을 조회하는 API입니다.")
    public BaseResponse<SliceResponse<ClothRecommendListResponse>> recommendCategoryClothes(
            @Parameter(description = "이전 페이지의 옷ID (첫 요청 시 생략)") @RequestParam(required = false)
                    Long lastClothId,
            @Parameter(description = "페이지당 조회할 옷 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "상위 카테고리 ID") @RequestParam Long categoryId,
            @Parameter(description = "요청 계절") @RequestParam Season season) {
        SliceResponse<ClothRecommendListResponse> response =
                clothService.recommendCategoryClothes(lastClothId, size, categoryId, season);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping
    @Operation(
            operationId = "Cloth_getClothes",
            summary = "옷 목록 조회",
            description = "옷장에서 옷 목록을 조회하는 API입니다.")
    public BaseResponse<SliceResponse<ClothListResponse>> getClothes(
            @Parameter(description = "이전 페이지의 옷 ID(첫 요청 시 생략)") @RequestParam(required = false)
                    Long lastClothId,
            @Parameter(description = "페이지당 조회할 옷의 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction,
            @Parameter(description = "옷의 카테고리 조건 (전체 조회시 생략)") @RequestParam(required = false)
                    Long categoryId,
            @Parameter(description = "옷의 계절 조건 (전체 조회시 생략)") @RequestParam(required = false)
                    List<Season> seasons) {
        SliceResponse<ClothListResponse> response =
                clothService.getClothes(lastClothId, size, direction, categoryId, seasons);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/{clothId}")
    @Operation(
            operationId = "Cloth_getClothDetails",
            summary = "옷 상세 조회",
            description = "옷을 상세 조회하는 API입니다.")
    public BaseResponse<ClothDetailsResponse> getClothDetails(@PathVariable Long clothId) {
        ClothDetailsResponse response = clothService.getClothDetails(clothId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @PatchMapping("/{clothId}")
    @Operation(operationId = "Cloth_updateCloth", summary = "옷 수정", description = "옷을 수정하는 API입니다.")
    public BaseResponse<Void> updateCloth(
            @PathVariable Long clothId, @RequestBody @Valid ClothUpdateRequest request) {
        clothService.updateCloth(clothId, request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @DeleteMapping("/{clothId}")
    @Operation(operationId = "Cloth_deleteCloth", summary = "옷 삭제", description = "옷을 삭제합니다.")
    public BaseResponse<Void> deleteCloth(@PathVariable Long clothId) {
        clothService.deleteCloth(clothId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }
}
