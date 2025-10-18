package org.clokey.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.BlockedMemberResponse;
import org.clokey.domain.member.dto.response.DuplicatedIdCheckResponse;
import org.clokey.domain.member.dto.response.MyselfCheckResponse;
import org.clokey.domain.member.service.MemberService;
import org.clokey.global.annotation.PageSize;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "11. 멤버 API", description = "멤버 관련 API입니다.")
@Validated
public class MemberController {

    private final MemberService memberService;

    @PatchMapping
    @Operation(summary = "프로필 수정", description = "프로필을 수정/추가 합니다.")
    public BaseResponse<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        memberService.updateProfile(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @PostMapping("/check-duplicate-id")
    @Operation(summary = "아이디 중복확인", description = "클로키아이디 중복을 확인합니다.")
    public BaseResponse<DuplicatedIdCheckResponse> checkDuplicateClokeyId(
            @Valid @RequestBody DuplicatedIdCheckRequest request) {

        DuplicatedIdCheckResponse response = memberService.checkDuplicateClokeyId(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @PostMapping("/follow")
    @Operation(summary = "팔로우 API", description = "다른 사용자를 팔로우/취소하는 API입니다. 공개 계정에 팔로우시 팔로우 됩니다.")
    public BaseResponse<Void> toggleFollow(@RequestParam("userId") Long userId) {

        memberService.toggleFollow(userId);

        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @PostMapping("/pending-follow")
    @Operation(
            summary = "팔로우 API",
            description = "다른 사용자를 팔로우/취소하는 API입니다. 비공개 계정에 팔로우시 요청이 들어갑니다.")
    public BaseResponse<Void> togglePendingFollow(@RequestParam("userId") Long userId) {

        memberService.togglePendingFollow(userId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @PostMapping("/block/{memberId}")
    @Operation(summary = "차단 토글 API", description = "차단 상태를 변경합니다.")
    public BaseResponse<Void> toggleBlockStatus(@PathVariable Long memberId) {
        memberService.toggleBlockStatus(memberId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping("/check-myself")
    @Operation(summary = "본인인지 여부 확인", description = "클로키 아이디로 본인인지 확인합니다.")
    public BaseResponse<MyselfCheckResponse> checkIsMySelf(
            @Parameter(description = "본인인지 확인할 클로키 ID") @RequestParam("clokeyId") String clokeyId) {

        return BaseResponse.onSuccess(
                GlobalBaseSuccessCode.OK, memberService.checkIsMyself(clokeyId));
    }

    @GetMapping("/blocks")
    @Operation(summary = "차단한 멤버 조회", description = "사용자가 차단한 모든 멤버들을 조회합니다.")
    public BaseResponse<SliceResponse<BlockedMemberResponse>> getBlockedMembers(
            @Parameter(description = "이전 페이지의 마지막 Block ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastBlockId,
            @Parameter(description = "페이지당 조회할 멤버 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction) {
        SliceResponse<BlockedMemberResponse> blockedMembersSlice =
                memberService.getBlockedMembers(lastBlockId, size, direction);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, blockedMembersSlice);
    }
}
