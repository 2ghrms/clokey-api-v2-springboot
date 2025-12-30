package org.clokey.domain.lookbook.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.lookbook.dto.request.LookBookCreateRequest;
import org.clokey.domain.lookbook.dto.request.LookBookUpdateRequest;
import org.clokey.domain.lookbook.dto.response.CoordinateListResponse;
import org.clokey.domain.lookbook.dto.response.LookBookCreateResponse;
import org.clokey.domain.lookbook.dto.response.LookBookListResponse;
import org.clokey.domain.lookbook.service.LookBookService;
import org.clokey.global.annotation.PageSize;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lookbooks")
@RequiredArgsConstructor
@Tag(name = "10. 룩북 API", description = "룩북 관련 API입니다.")
@Validated
public class LookBookController {

    private final LookBookService lookBookService;

    @PostMapping()
    @Operation(
            operationId = "LookBook_createLookBook",
            summary = "룩북 생성",
            description = "룩북을 생성하는 API입니다.")
    public BaseResponse<LookBookCreateResponse> createLookBook(
            @Valid @RequestBody LookBookCreateRequest request) {
        LookBookCreateResponse response = lookBookService.createLookBook(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PatchMapping("/{lookBookId}")
    @Operation(
            operationId = "LookBook_updateLookBook",
            summary = "룩북 수정",
            description = "룩북을 수정하는 API입니다.")
    public BaseResponse<Void> updateLookBook(
            @PathVariable Long lookBookId, @Valid @RequestBody LookBookUpdateRequest request) {
        lookBookService.updateLookBook(lookBookId, request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @DeleteMapping("/{lookBookId}")
    @Operation(
            operationId = "LookBook_deleteLookBook",
            summary = "룩북 삭제",
            description = "룩북을 삭제하는 API입니다.")
    public BaseResponse<Void> deleteLookBook(@PathVariable Long lookBookId) {
        lookBookService.deleteLookBook(lookBookId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping()
    @Operation(
            operationId = "LookBook_getLookBooks",
            summary = "룩북 전체 목록 조회",
            description = "룩북 전체 목록을 조회하는 API입니다.")
    public BaseResponse<SliceResponse<LookBookListResponse>> getLookBooks(
            @Parameter(description = "이전 페이지의 마지막 룩북 ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastLookBookId,
            @Parameter(description = "페이지당 조회할 룩북의 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction) {
        SliceResponse<LookBookListResponse> response =
                lookBookService.getLookBooks(lastLookBookId, size, direction);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/{lookBookId}")
    @Operation(
            operationId = "LookBook_getCoordinates",
            summary = "개별 룩북 코디 목록 조회",
            description = "개별 룩북 내부의 코디들을 API입니다.")
    public BaseResponse<SliceResponse<CoordinateListResponse>> getCoordinates(
            @PathVariable Long lookBookId,
            @Parameter(description = "이전 페이지의 마지막 코디 ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastCoordinateId,
            @Parameter(description = "페이지당 조회할 코디의 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction) {
        SliceResponse<CoordinateListResponse> response =
                lookBookService.getCoordinates(lookBookId, lastCoordinateId, size, direction);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
}
