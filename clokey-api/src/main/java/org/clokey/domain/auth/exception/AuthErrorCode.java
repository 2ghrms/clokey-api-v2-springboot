package org.clokey.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    AUTH_NOT_EXIST(401, "AUTH_4011", "인증 정보가 존재하지 않습니다."),
    AUTH_NOT_PARSABLE(500, "AUTO_5001", "인증 정보 파싱에 실패했습니다."),

    REFRESH_TOKEN_NOT_FOUND(404, "AUTH_4041", "리프레시 토큰을 찾지 못했습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
