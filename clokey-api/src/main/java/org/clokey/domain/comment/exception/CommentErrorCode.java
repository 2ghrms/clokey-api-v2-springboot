package org.clokey.domain.comment.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.clokey.dto.ErrorReasonDto;
import org.clokey.exception.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements BaseErrorCode {
    REPLY_NOT_FROM_COMMENT(400, "COMMENT_4001", "댓글에 속하는 대댓글이 아닙니다."),

    NOT_MY_COMMENT(403, "COMMENT_4031", "내가 작성하지 않은 댓글은 삭제할 수 없습니다."),
    NOT_MY_REPLY(403, "COMMENT_4032", "내가 작성하지 않은 대댓글은 삭제할 수 없습니다."),

    COMMENT_NOT_FOUND(404, "COMMENT_4041", "존재하지 않는 댓글입니다."),
    REPLY_NOT_FOUND(404, "COMMENT_4042", "존재하지 않는 대댓글입니다.");

    private final int status;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getErrorReason() {
        return ErrorReasonDto.of(status, code, message);
    }
}
