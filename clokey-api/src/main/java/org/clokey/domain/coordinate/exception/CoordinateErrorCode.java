package org.clokey.domain.coordinate.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CoordinateErrorCode implements BaseErrorCode {
    DAILY_COORDINATE_ALREADY_EXISTS(400, "COORDINATE_4001", "오늘의 코디가 이미 존재합니다"),
    INVALID_ORDER(400, "COORDINATE_4002", "옷들의 순서가 1부터 시작하며 연속되는 조건에 부합하지 않습니다."),
    CLOTHES_OVER_COORDINATION_LIMIT(400, "COORDINATE_4003", "코디에는 옷을 10개까지만 등록 가능합니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
