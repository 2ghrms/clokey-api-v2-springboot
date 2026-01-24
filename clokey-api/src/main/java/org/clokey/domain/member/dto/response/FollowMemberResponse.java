package org.clokey.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FollowMemberResponse(
        @Schema(description = "팔로우의 ID", example = "1") Long followId,
        @Schema(description = "팔로잉 or 팔로워의 Member의 Id", example = "1") Long memberId,
        @Schema(description = "팔로잉 or 팔로워의 닉네임", example = "clokey123") String nickname,
        @Schema(
                        description = "팔로잉 or 팔로워의 PofileImageUrl",
                        example = "https://example.com/profile/john.jpg")
                String profileImageUrl,
        @Schema(description = "요청자가 이 멤버를 팔로우하고 있는가?", example = "true") boolean isFollowingMember,
        @Schema(description = "해당 멤버가 본인(요청자)인가?", example = "false") boolean isMe) {}
