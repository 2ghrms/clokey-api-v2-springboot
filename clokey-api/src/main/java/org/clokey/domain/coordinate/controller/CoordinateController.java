package org.clokey.domain.coordinate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.coordinate.dto.request.CoordinateAutoCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateManualCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateUpdateRequest;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.*;
import org.clokey.domain.coordinate.service.CoordinateService;
import org.clokey.global.annotation.PageSize;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.beans.TypeMismatchException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coordinate")
@RequiredArgsConstructor
@Tag(name = "05. 코디 API", description = "코디 관련 API입니다.")
@Validated
public class CoordinateController {

    private final CoordinateService coordinateService;

    @PostMapping("/daily")
    @Operation(
            operationId = "Coordinate_createDailyCoordinate",
            summary = "오늘의 코디 생성",
            description = "홈화면에서 오늘의 코디를 생성하는 API입니다.")
    public BaseResponse<CoordinateCreateResponse> createDailyCoordinate(
            @Valid @RequestBody DailyCoordinateCreateRequest request) {
        CoordinateCreateResponse response = coordinateService.createDailyCoordinate(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/manual")
    @Operation(
            operationId = "Coordinate_createCoordinateManual",
            summary = "코디 수동 생성",
            description = "코디 생성을 수동으로 하는 API입니다.")
    public BaseResponse<CoordinateCreateResponse> createCoordinateManual(
            @Valid @RequestBody CoordinateManualCreateRequest request) {
        CoordinateCreateResponse response = coordinateService.createCoordinateManual(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/auto")
    @Operation(
            operationId = "Coordinate_createCoordinateAuto",
            summary = "코디 자동 생성",
            description = "오늘의 코디를 통해서 코디를 생성하는 API입니다.")
    public BaseResponse<CoordinateCreateResponse> createCoordinateAuto(
            @Valid @RequestBody CoordinateAutoCreateRequest request) {
        CoordinateCreateResponse response = coordinateService.createCoordinateAuto(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PatchMapping("/{coordinateId}")
    @Operation(
            operationId = "Coordinate_updateCoordinate",
            summary = "코디 수정",
            description = "코디를 수정하는 API입니다.")
    public BaseResponse<Void> updateCoordinate(
            @PathVariable Long coordinateId, @Valid @RequestBody CoordinateUpdateRequest request) {
        coordinateService.updateCoordinate(coordinateId, request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @DeleteMapping("/{coordinateId}")
    @Operation(
            operationId = "Coordinate_deleteCoordinate",
            summary = "코디 삭제",
            description = "코디를 룩북에서 삭제하는 API입니다.")
    public BaseResponse<Void> deleteCoordinate(@PathVariable Long coordinateId) {
        coordinateService.deleteCoordinate(coordinateId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping("/daily")
    @Operation(
            operationId = "Coordinate_getDailyCoordinates",
            summary = "과거 일일 코디 조회",
            description = "룩북 추가를 위해 과거 일일 코디를 조회하는 API입니다.")
    public BaseResponse<SliceResponse<DailyCoordinateListResponse>> getDailyCoordinates(
            @Parameter(description = "이전 페이지의 마지막 코디 ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastCoordinateId,
            @Parameter(description = "페이지당 조회할 코디 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction) {
        SliceResponse<DailyCoordinateListResponse> response =
                coordinateService.getDailyCoordinates(lastCoordinateId, size, direction);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/daily/today/preview")
    @Operation(
            operationId = "Coordinate_getTodayCoordinatePreview",
            summary = "오늘의 코디 Preview 조회",
            description = "오늘의 코디의 Preview를 조회하는 API입니다.")
    public BaseResponse<DailyCoordinatePreviewResponse> getTodayCoordinatePreview() {
        DailyCoordinatePreviewResponse response = coordinateService.getTodayCoordinatePreview();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/daily/today/details")
    @Operation(
            operationId = "Coordinate_getTodayCoordinateDetails",
            summary = "오늘의 코디 Details 조회",
            description = "오늘의 코디의 Details를 조회하는 API입니다.")
    public BaseResponse<List<CoordinateDetailsListResponse>> getTodayCoordinateDetails() {
        List<CoordinateDetailsListResponse> response =
                coordinateService.getTodayCoordinateDetails();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/{coordinateId}/preview")
    @Operation(
            operationId = "Coordinate_getCoordinatePreview",
            summary = "코디 Preview 조회",
            description = "룩북에 존재하는 코디의 Preview를 조회하는 API입니다.")
    public BaseResponse<CoordinatePreviewResponse> getCoordinatePreview(
            @PathVariable Long coordinateId) {
        CoordinatePreviewResponse response = coordinateService.getCoordinatePreview(coordinateId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/{coordinateId}/details")
    @Operation(
            operationId = "Coordinate_getCoordinateDetails",
            summary = "코디 Details 조회",
            description = "룩북에 존재하는 코디의 Details를 조회하는 API입니다.")
    public BaseResponse<List<CoordinateDetailsListResponse>> getCoordinateDetails(
            @PathVariable Long coordinateId) {
        List<CoordinateDetailsListResponse> response =
                coordinateService.getCoordinateDetails(coordinateId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @PatchMapping("/{coordinateId}/like")
    @Operation(
            operationId = "Coordinate_toggleCoordinateLike",
            summary = "코디 좋아요 토글",
            description = "룩북에 존재하는 코디에 좋아요를 토글하는 API입니다.")
    public BaseResponse<Void> toggleCoordinateLike(@PathVariable Long coordinateId) {
        coordinateService.toggleCoordinateLike(coordinateId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping("/favorites")
    @Operation(
            operationId = "Coordinate_getFavoriteCoordinates",
            summary = "최애 코디 조회",
            description = "최애 코디를 조회하는 API입니다.")
    public BaseResponse<List<FavoriteCoordinateResponse>> getFavoriteCoordinates(
            @RequestParam(required = false) String memberId) {
        Long parsedMemberId = null;
        if (memberId != null && !memberId.isBlank()) {
            try {
                parsedMemberId = Long.valueOf(memberId);
            } catch (NumberFormatException e) {
                throw new TypeMismatchException(memberId, Long.class, e);
            }
        }
        List<FavoriteCoordinateResponse> response =
                coordinateService.getFavoriteCoordinates(parsedMemberId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
}
