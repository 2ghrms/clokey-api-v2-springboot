package org.clokey.domain.lookbook.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum LookBookErrorCode implements BaseErrorCode {
    NOT_LOOK_BOOK_OWNER(400, "LOOK_BOOK_4031", "나의 룩북이 아닙니다. 권한이 없습니다."),

    LOOK_BOOK_NOT_FOUND(400, "LOOK_BOOK_4041", "존재하지 않는 룩북입니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
