package org.clokey.domain.like.event;

public record NewLikeEvent(
        Long likeId,
        Long historyId,
        Long receiverId,
        Long likerId,
        String likerNickname,
        String likerProfileImageUrl) {}
