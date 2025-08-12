package org.clokey.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.comment.entitiy.Reply;

public record ReplyCreateResponse(
        @Schema(description = "작성된 대댓글의 ID", example = "1") Long replyId) {
    public static ReplyCreateResponse from(Reply reply) {
        return new ReplyCreateResponse(reply.getId());
    }
}
