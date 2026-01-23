package org.clokey.domain.like.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.dto.response.LikedMembersResponse;
import org.clokey.domain.like.service.LikeService;
import org.clokey.global.annotation.PageSize;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "09. 좋아요 API", description = "좋아요 관련 API입니다.")
@Validated
public class LikeController {

    private final LikeService likeService;

    @GetMapping("/users")
    @Operation(
            operationId = "Like_getLikedMembers",
            summary = "좋아요한 유저 조회",
            description = "내 기록을 좋아요한 유저를 조회합니다")
    public BaseResponse<SliceResponse<LikedMembersResponse.LikedMemberPreview>> getLikedMembers(
            @Parameter(description = "기록 ID") @RequestParam Long historyId,
            @Parameter(description = "이전 페이지의 좋아요 ID (첫 요청 시 생략)") @RequestParam(required = false)
                    Long lastLikeId,
            @Parameter(description = "페이지당 조회할 개수") @RequestParam @PageSize Integer size) {
        SliceResponse<LikedMembersResponse.LikedMemberPreview> response =
                likeService.getLikedMembers(historyId, lastLikeId, size);

        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/histories")
    @Operation(
            operationId = "Like_getLikedHistories",
            summary = "좋아요한 기록 조회",
            description = "사용자가 좋아요한 기록을 조회합니다.")
    public BaseResponse<SliceResponse<LikedHistoriesResponse.LikedHistoryPreview>>
            getLikedHistories(
                    @Parameter(description = "이전 페이지의 좋아요 ID (첫 요청 시 생략)")
                            @RequestParam(required = false)
                            Long lastLikeId,
                    @Parameter(description = "페이지당 조회할 개수") @RequestParam @PageSize Integer size) {
        SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> response =
                likeService.getLikedHistories(lastLikeId, size);

        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @PostMapping
    @Operation(
            operationId = "Like_toggleLike",
            summary = "좋아요 추가 및 취소",
            description = "기록에 좋아요를 추가 및 취소합니다. 토글 방식으로 동작합니다.")
    public BaseResponse<Void> toggleLike(@RequestParam("historyId") Long historyId) {

        likeService.toggleLike(historyId);

        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }
}
