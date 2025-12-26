package org.clokey.domain.report.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {
    REPORT_DUPLICATED(400, "REPORT_4001", "신고가 접수되어 운영 정책 위반 여부를 확인하고 있는 콘텐츠입니다."),

    REPORT_NOT_FOUND(404, "COMMENT_4041", "존재하지 않는 신고입니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
