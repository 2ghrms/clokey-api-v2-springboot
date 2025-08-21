package org.clokey.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    BANNED_MEMBER_TO_PUBLIC(400, "MEMBER_4001", "신고당한 회원은 공개로 전환할 수 없습니다."),
    MEMBER_NOT_FOUND(404, "MEMBER_4041", "해당 회원을 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return org.clokey.dto.ErrorReasonDto.of(status, code, message);
    }
}
