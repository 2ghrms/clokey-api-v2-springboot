package org.clokey.domain.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "FeedListResponse", description = "피드 목록 응답")
public record FeedListResponse(
        @Schema(description = "피드 목록") List<FeedItemResponse> items,
        @Schema(description = "다음 페이지 커서", example = "Y3JlYXRlZEF0fDEwMA==") String nextCursor,
        @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext) {
    public static FeedListResponse of(
            List<FeedItemResponse> items, String nextCursor, boolean hasNext) {
        return new FeedListResponse(items, nextCursor, hasNext);
    }

    @Schema(name = "FeedItemResponse", description = "피드 단일 항목")
    public record FeedItemResponse(
            @Schema(description = "피드 ID", example = "100") Long feedId,
            @Schema(description = "작성 시각", example = "2025-01-01T12:00:00") LocalDateTime createdAt,
            @Schema(description = "대표 이미지 URL", example = "https://example.com/image.jpg")
                    String imageUrl,
            @Schema(description = "좋아요 여부", example = "true") boolean isLiked,
            @Schema(description = "작성자 정보") FeedAuthorResponse author) {}

    @Schema(name = "FeedAuthorResponse", description = "피드 작성자 정보")
    public record FeedAuthorResponse(
            @Schema(description = "작성자 ID", example = "1") Long memberId,
            @Schema(description = "클로키 ID", example = "clokey123") String clokeyId,
            @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
                    String profileImageUrl,
            @Schema(description = "팔로잉 여부", example = "true") boolean isFollowing) {}
}
