package org.clokey.domain.history.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum HistoryErrorCode implements BaseErrorCode {
    HISTORY_NOT_FOUND(404, "HISTORY_4041", "존재하지 않는 기록입니다."),
    LIMITED_AUTHORITY(403, "HISTORY_4031", "기록에 대한 접근 권한이 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
