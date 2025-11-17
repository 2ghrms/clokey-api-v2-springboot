package org.clokey.domain.comment.event;

public record NewCommentEvent(
        Long commentId,
        Long historyId,
        Long receiverId,
        Long commenterId,
        String commenterNickname,
        String commenterProfileImageUrl,
        String commentContent) {}
