package org.clokey.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.DuplicatedIdCheckResponse;
import org.clokey.domain.member.service.MemberService;
import org.clokey.response.BaseResponse;
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

    @PostMapping("/block/{memberId}")
    @Operation(summary = "차단 토글 API", description = "차단 상태를 변경합니다.")
    public BaseResponse<Void> toggleBlockStatus(@PathVariable Long memberId) {
        memberService.toggleBlockStatus(memberId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }
}
