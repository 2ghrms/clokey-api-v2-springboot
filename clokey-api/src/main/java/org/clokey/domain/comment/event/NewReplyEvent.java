package org.clokey.domain.comment.event;

public record NewReplyEvent(
        Long replyId,
        Long historyId,
        Long parentCommentId,
        Long receiverId,
        Long replierId,
        String replierNickname,
        String replierProfileImageUrl,
        String replyContent) {}
