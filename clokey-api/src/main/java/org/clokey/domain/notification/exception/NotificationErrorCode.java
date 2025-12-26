package org.clokey.domain.notification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {
    NOTIFICATION_NOT_FOUND(404, "NOTIFICATION_4041", "해당 알림을 찾을 수 없습니다."),

    NOTIFICATION_FIREBASE_ERROR(500, "NOTIFICATION_5001", "FCM 전송 실패 예외입니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
