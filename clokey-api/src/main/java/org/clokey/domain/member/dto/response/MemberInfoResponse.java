package org.clokey.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberInfoResponse(
        @Schema(description = "조회할 멤버의 Codive ID", example = "codive123") String codiveId,
        @Schema(description = "조회할 멤버의 닉네임", example = "닉네임123") String nickname,
        @Schema(description = "조회할 멤버의 한줄소개", example = "코디브 너무 좋아~~ㅋㅋ") String bio,
        @Schema(description = "조회할 멤버의 팔로워 수", example = "150") Long followerCount,
        @Schema(description = "조회할 멤버의 팔로잉 수", example = "150") Long followingCount,
        @Schema(
                        description = "조회할 멤버의 프로필 이미지 URL",
                        example = "https://example.com/profile/john.jpg")
                String profileImageUrl,
        @Schema(description = "요청자가 조회할 멤버를 팔로우하고 있는지?", example = "true") boolean isFollowing,
        @Schema(description = "조회할 멤버가 요청자 본인인지?", example = "false") boolean isMe) {}
