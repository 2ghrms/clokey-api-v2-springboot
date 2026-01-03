package org.clokey.domain.history.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum HistoryErrorCode implements BaseErrorCode {
    BANNED_HISTORY(400, "HISTORY_4001", "신고당한 기록은 조회할 수 없습니다."),

    LIMITED_AUTHORITY(403, "HISTORY_4031", "기록에 대한 접근 권한이 없습니다."),
    BLOCKED_AUTHORITY(403, "HISTORY_4032", "기록 작성자를 차단했거나 차단 당했습니다"),

    HISTORY_NOT_FOUND(404, "HISTORY_4041", "존재하지 않는 기록입니다."),
    HISTORY_IMAGE_NOT_FOUND(404, "HISTORY_4042", "존재하지 않는 기록 이미지입니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
