package org.clokey.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.notification.dto.response.NotificationListResponse;
import org.clokey.domain.notification.dto.response.UnreadNotificationResponse;
import org.clokey.domain.notification.service.CodiveNotificationService;
import org.clokey.global.annotation.PageSize;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "12. 알림 API", description = "푸시 알림 관련 API입니다.")
@Validated
public class CodiveNotificationController {

    private final CodiveNotificationService codiveNotificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "회원의 알림 목록을 조회합니다")
    public BaseResponse<SliceResponse<NotificationListResponse>> getNotificationList(
            @Parameter(description = "이전 페이지의 마지막 CodiveNotification ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastNotificationId,
            @Parameter(description = "페이지당 조회할 알림 수") @RequestParam @PageSize Integer size) {
        return BaseResponse.onSuccess(
                GlobalBaseSuccessCode.OK,
                codiveNotificationService.getNotificationList(lastNotificationId, size));
    }

    @GetMapping("not-read-exist")
    @Operation(summary = "안읽은 알림 존재 유무 확인", description = "안읽음 상태인 알림 존재 유무를 확인합니다.")
    public BaseResponse<UnreadNotificationResponse> existsUnreadNotification() {
        return BaseResponse.onSuccess(
                GlobalBaseSuccessCode.OK, codiveNotificationService.existsUnreadNotification());
    }
}
