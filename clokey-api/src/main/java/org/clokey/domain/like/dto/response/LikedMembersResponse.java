package org.clokey.domain.like.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

@Schema(description = "좋아요 유저 조회 결과")
public record LikedMembersResponse(
        @Schema(description = "유저 미리보기 목록") List<LikedMemberPreview> memberPreviews,
        @Schema(description = "마지막 페이지 여부", example = "false") boolean isLast) {

    @Schema(description = "유저 미리보기 DTO")
    @Getter
    public static class LikedMemberPreview {
        @Schema(description = "유저 ID", example = "30")
        private final Long id;

        @Schema(description = "프로필 이미지 URL")
        private final String imageUrl;

        @Schema(description = "닉네임")
        private final String nickname;

        @Schema(description = "팔로우 여부")
        private final boolean followStatus;

        @Schema(description = "다음 페이지 조회를 위한 커서 ID (MemberLike ID)", example = "100")
        private final Long lastLikeId;

        public LikedMemberPreview(Long id, String imageUrl, String nickname, Long lastLikeId) {
            this.id = id;
            this.imageUrl = imageUrl;
            this.nickname = nickname;
            this.lastLikeId = lastLikeId;
            this.followStatus = false;
        }

        public LikedMemberPreview(Long id, String imageUrl, String nickname, boolean followStatus) {
            this.id = id;
            this.imageUrl = imageUrl;
            this.nickname = nickname;
            this.followStatus = followStatus;
            this.lastLikeId = null;
        }
    }
}
