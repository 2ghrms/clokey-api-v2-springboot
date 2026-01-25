package org.clokey.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.*;
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
    @Operation(
            operationId = "Member_updateProfile",
            summary = "프로필 수정",
            description = "프로필을 수정/추가 합니다.")
    public BaseResponse<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        memberService.updateProfile(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @PostMapping("/check-duplicate-nickname")
    @Operation(
            operationId = "Member_checkDuplicateNickname",
            summary = "닉네임 중복확인",
            description = "닉네임 중복을 확인합니다.")
    public BaseResponse<DuplicatedIdCheckResponse> checkDuplicateNickname(
            @Valid @RequestBody DuplicatedIdCheckRequest request) {

        DuplicatedIdCheckResponse response = memberService.checkDuplicateNickname(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @PostMapping("/follow")
    @Operation(
            operationId = "Member_toggleFollow",
            summary = "팔로우 API",
            description = "다른 사용자를 팔로우/취소하는 API입니다. 공개 계정에 팔로우시 팔로우 됩니다.")
    public BaseResponse<Void> toggleFollow(@RequestParam("userId") Long userId) {

        memberService.toggleFollow(userId);

        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @PostMapping("/pending-follow")
    @Operation(
            operationId = "Member_togglePendingFollow",
            summary = "팔로우 API",
            description = "다른 사용자를 팔로우/취소하는 API입니다. 비공개 계정에 팔로우시 요청이 들어갑니다.")
    public BaseResponse<Void> togglePendingFollow(@RequestParam("userId") Long userId) {

        memberService.togglePendingFollow(userId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @PostMapping("/block/{memberId}")
    @Operation(
            operationId = "Member_toggleBlockStatus",
            summary = "차단 토글 API",
            description = "차단 상태를 변경합니다.")
    public BaseResponse<Void> toggleBlockStatus(@PathVariable Long memberId) {
        memberService.toggleBlockStatus(memberId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping("/check-myself")
    @Operation(
            operationId = "Member_checkIsMySelf",
            summary = "본인인지 여부 확인",
            description = "닉네임으로 본인인지 확인합니다.")
    public BaseResponse<MyselfCheckResponse> checkIsMySelf(
            @Parameter(description = "본인인지 확인할 닉네임") @RequestParam("nickname") String nickname) {

        return BaseResponse.onSuccess(
                GlobalBaseSuccessCode.OK, memberService.checkIsMyself(nickname));
    }

    @GetMapping("/blocks")
    @Operation(
            operationId = "Member_getBlockedMembers",
            summary = "차단한 멤버 조회",
            description = "사용자가 차단한 모든 멤버들을 조회합니다.")
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

    @GetMapping("/follows")
    @Operation(
            operationId = "Member_getFollows",
            summary = "팔로잉/팔로워 멤버 조회",
            description = "해당 사용자의  모든 팔로잉 OR 팔로워들을 조회합니다.")
    public BaseResponse<SliceResponse<FollowMemberResponse>> getFollows(
            @Parameter(description = "목록을 조회할 멤버의 Member ID") @RequestParam Long memberId,
            @Parameter(description = "이전 페이지의 마지막 Follow ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastFollowId,
            @Parameter(description = "팔로잉 요청인지/팔로워 요청인지") @RequestParam boolean isFollowing,
            @Parameter(description = "페이지당 조회할 멤버 수") @RequestParam @PageSize Integer size) {
        SliceResponse<FollowMemberResponse> followMembersSlice =
                memberService.getFollows(memberId, lastFollowId, isFollowing, size);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, followMembersSlice);
    }

    @GetMapping("{memberId}")
    @Operation(
            operationId = "Member_getMemberInfo",
            summary = "회원 조회 API",
            description = "입력받은 member ID에 해당하는 회원의 정보를 조회합니다.")
    public BaseResponse<MemberInfoResponse> getMemberInfo(@PathVariable Long memberId) {
        return BaseResponse.onSuccess(
                GlobalBaseSuccessCode.OK, memberService.getMemberInfo(memberId));
    }

    @GetMapping("/me")
    @Operation(
            operationId = "Member_getMyInfo",
            summary = "내 정보 조회 API",
            description = "로그인한 사용자의 본인 정보를 조회합니다.")
    public BaseResponse<MyInfoResponse> getMyInfo() {
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, memberService.getMyInfo());
    }
}
