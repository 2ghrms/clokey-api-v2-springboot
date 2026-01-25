package org.clokey.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyInfoResponse(
        @Schema(description = "조회할 멤버의 memberId", example = "1L") Long memberId,
        @Schema(description = "조회할 멤버의 닉네임", example = "닉네임123") String nickname,
        @Schema(description = "조회할 멤버의 한줄소개", example = "코디브 너무 좋아~~ㅋㅋ") String bio,
        @Schema(description = "조회할 멤버의 이메일", example = "codive@example.com") String email,
        @Schema(description = "조회할 멤버의 팔로워 수", example = "150") Long followerCount,
        @Schema(description = "조회할 멤버의 팔로잉 수", example = "150") Long followingCount,
        @Schema(
                        description = "조회할 멤버의 프로필 이미지 URL",
                        example = "https://example.com/profile/john.jpg")
                String profileImageUrl,
        @Schema(description = "조회할 멤버가 공개 계정인지?", example = "true") boolean isPublic,
        @Schema(description = "조회할 멤버가 요청자 본인인지?", example = "true") boolean isMe) {}
