package org.clokey.domain.history.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.request.HistoryImagesUploadRequest;
import org.clokey.domain.history.dto.request.HistoryUpdateRequest;
import org.clokey.domain.history.dto.response.DailyHistoryResponse;
import org.clokey.domain.history.dto.response.HistoryClothTagListResponse;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.dto.response.HistoryImagesPresignedUrlResponse;
import org.clokey.domain.history.dto.response.HistoryOwnershipCheckResponse;
import org.clokey.domain.history.dto.response.MonthlyHistoryResponse;
import org.clokey.domain.history.dto.response.SituationListResponse;
import org.clokey.domain.history.dto.response.StyleListResponse;
import org.clokey.domain.history.service.HistoryService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/histories")
@RequiredArgsConstructor
@Tag(name = "07. 기록 API", description = "기록 관련 API입니다.")
@Validated
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping("/images")
    @Operation(
            operationId = "History_getHistoryUploadPresignedUrl",
            summary = "기록 이미지 업로드용 presignedUrl 발급",
            description = "기록 이미지 업로드용 presignedUrl을 발급합니다.")
    public BaseResponse<HistoryImagesPresignedUrlResponse> getHistoryUploadPresignedUrl(
            @Valid @RequestBody HistoryImagesUploadRequest request) {
        HistoryImagesPresignedUrlResponse response =
                historyService.getHistoryUploadPresignedUrls(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping
    @Operation(
            operationId = "History_createHistory",
            summary = "기록 생성",
            description = "새로운 기록을 생성합니다.")
    public BaseResponse<HistoryCreateResponse> createHistory(
            @Valid @RequestBody HistoryCreateRequest request) {
        HistoryCreateResponse response = historyService.createHistory(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PatchMapping("/{historyId}")
    @Operation(
            operationId = "History_updateHistory",
            summary = "기록 수정",
            description = "기록을 수정하는 API입니다")
    public BaseResponse<Void> updateHistory(
            @PathVariable Long historyId, @Valid @RequestBody HistoryUpdateRequest request) {
        historyService.updateHistory(historyId, request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping("/styles")
    @Operation(
            operationId = "History_getAllStyles",
            summary = "스타일 목록 조회",
            description = "스타일 목록을 조회하는 API입니다. (기록 생성용)")
    public BaseResponse<StyleListResponse> getAllStyles() {
        StyleListResponse response = historyService.getAllStyles();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/situations")
    @Operation(
            operationId = "History_getAllSituations",
            summary = "상황 목록 조회",
            description = "상황 목록을 조회하는 API입니다. (기록 생성용)")
    public BaseResponse<SituationListResponse> getAllSituations() {
        SituationListResponse response = historyService.getAllSituations();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/{historyId}")
    @Operation(
            operationId = "History_getHistoryDetails",
            summary = "일별 기록 조회",
            description = "기록 ID를 통해 일별 기록의 정보를 조회합니다.")
    public BaseResponse<DailyHistoryResponse> getDailyHistory(@PathVariable Long historyId) {
        DailyHistoryResponse response = historyService.getDailyHistory(historyId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/cloth-tag/{historyImageId}")
    @Operation(
            operationId = "History_getHistoryClothTags",
            summary = "기록 이미지의 옷 태그 조회",
            description = "기록 이미지 ID를 통해 해당 이미지에 태그된 옷들의 정보와 위치를 조회합니다.")
    public BaseResponse<HistoryClothTagListResponse> getHistoryClothTags(
            @PathVariable Long historyImageId) {
        HistoryClothTagListResponse response = historyService.getHistoryClothTags(historyImageId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/monthly/{memberId}")
    @Operation(
            operationId = "History_getMonthlyHistory",
            summary = "월별 기록 조회",
            description = "특정 회원의 특정 년도/월에 해당하는 모든 기록의 ID와 첫 번째 이미지 URL을 조회합니다.")
    public BaseResponse<MonthlyHistoryResponse> getMonthlyHistory(
            @PathVariable Long memberId, @RequestParam int year, @RequestParam int month) {
        MonthlyHistoryResponse response = historyService.getMonthlyHistory(memberId, year, month);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/{historyId}/ownership")
    @Operation(
            operationId = "History_checkHistoryOwnership",
            summary = "나의 기록 여부 확인",
            description = "기록 ID를 통해 해당 기록이 현재 사용자의 기록인지 확인합니다.")
    public BaseResponse<HistoryOwnershipCheckResponse> checkHistoryOwnership(
            @PathVariable Long historyId) {
        HistoryOwnershipCheckResponse response = historyService.checkHistoryOwnership(historyId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @DeleteMapping("/{historyId}")
    @Operation(
            operationId = "History_deleteHistory",
            summary = "기록 삭제",
            description = "기록 ID를 통해 기록을 삭제합니다.")
    public BaseResponse<Void> deleteHistory(@PathVariable Long historyId) {
        historyService.deleteHistory(historyId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }
}
