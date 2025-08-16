package org.clokey.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.auth.dto.response.UserStatusResponse;
import org.clokey.domain.auth.service.AuthService;
import org.clokey.response.BaseResponse;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1-1. 인증 API", description = "인증 관련 API입니다.")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/my-status")
    @Operation(summary = "회원 상태 확인", description = "가입 완료 or 약관 동의 여부 확인 가능.")
    public BaseResponse<UserStatusResponse> getUserStatus() {
        UserStatusResponse response = authService.getUserStatus();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
}
