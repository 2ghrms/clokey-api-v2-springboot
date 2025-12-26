package org.clokey.domain.term.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum TermErrorCode implements BaseErrorCode {
    TERMS_MISMATCH(400, "TERM_4001", "응답한 약관 ID들이 DB에 존재하는 약관ID들과 일치하지 않습니다."),
    NON_OPTIONAL_NOT_AGREED(400, "TERM_4002", "필수 약관에 동의하지 않았습니다."),

    TERM_NOT_FOUND(404, "TERM_4041", "약관이 존재하지 않습니다."),
    MEMBER_SKIPPED_TERM_AGREEMENT(404, "TERM_4042", "약관 동의 절차를 수행하지 않은 회원입니다."),
    NOT_OPTIONAL_TERM(404, "TERM_4043", "선택 약관ID가 아닙니다."),
    ;

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
