package org.clokey.domain.history.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum StyleErrorCode implements BaseErrorCode {
    STYLE_NOT_FOUND(404, "STYLE_4041", "존재하지 않는 스타일입니다."),
    DUPLICATED_STYLE(400, "STYLE_4001", "중복된 스타일이 존재합니다.");
    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
