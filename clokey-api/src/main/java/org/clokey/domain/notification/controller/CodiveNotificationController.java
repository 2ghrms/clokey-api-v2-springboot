package org.clokey.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.notification.dto.response.UnreadNotificationResponse;
import org.clokey.domain.notification.service.CodiveNotificationService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "12. 알림 API", description = "푸시 알림 관련 API입니다.")
@Validated
public class CodiveNotificationController {

    private final CodiveNotificationService codiveNotificationService;

    @GetMapping("not-read-exist")
    @Operation(summary = "안읽은 알림 존재 유무 확인", description = "안읽음 상태인 알림 존재 유무를 확인합니다.")
    public BaseResponse<UnreadNotificationResponse> existsUnreadNotification() {
        return BaseResponse.onSuccess(
                GlobalBaseSuccessCode.OK, codiveNotificationService.existsUnreadNotification());
    }
}
