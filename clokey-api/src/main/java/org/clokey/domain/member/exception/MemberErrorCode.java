package org.clokey.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    BANNED_MEMBER_TO_PUBLIC(400, "MEMBER_4001", "신고당한 회원은 공개로 전환할 수 없습니다."),
    SELF_BLOCK_UNAVAILABLE(400, "MEMBER_4002", "자기 자신을 차단할 수 없습니다"),
    CANNOT_FOLLOW_MYSELF(400, "MEMBER_4003", "자기 자신을 팔로우할 수 없습니다."),
    MUST_REQUEST_FOLLOW(400, "MEMBER_4004", "비공개 회원에게는 팔로우 요청을 보내야 합니다."),
    MUST_FOLLOW(400, "MEMBER_4005", "공개 회원에게는 팔로우를 보내야 합니다."),
    CANNOT_FOLLOW_BLOCKED(400, "MEMBER_4006", "차단을 했거나 상대방에게 차단을 당한 경우 팔로우 할 수 없습니다."),

    PRIVATE_MEMBER_ACCESS_DENIED(403, "MEMBER_4031", "비공개 회원의 리소스에 접근할 수 없습니다."),
    BLOCKED_MEMBER_ACCESS_DENIED(403, "MEMBER_4032", "차단된 사용자의 리소스에 접근할 수 없습니다"),

    MEMBER_NOT_FOUND(404, "MEMBER_4041", "해당 회원을 찾을 수 없습니다."),
    NICKNAME_NOT_FOUND(404, "MEMBER_4042", "해당 닉네임을 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
