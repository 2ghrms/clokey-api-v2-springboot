package org.clokey.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.comment.entitiy.Comment;

public record CommentCreateResponse(
        @Schema(description = "작성된 댓글의 ID", example = "1") Long commentId) {
    public static CommentCreateResponse from(Comment comment) {
        return new CommentCreateResponse(comment.getId());
    }
}
