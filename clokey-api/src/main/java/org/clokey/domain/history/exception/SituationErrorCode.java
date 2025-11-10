package org.clokey.domain.history.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum SituationErrorCode implements BaseErrorCode {
    SITUATION_NOT_FOUND(404, "SITUATION_4041", "존재하지 않는 상황입니다.");
    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
