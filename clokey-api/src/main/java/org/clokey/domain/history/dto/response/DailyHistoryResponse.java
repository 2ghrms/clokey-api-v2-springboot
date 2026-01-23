package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record DailyHistoryResponse(
        @Schema(description = "회원 ID", example = "1") Long memberId,
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
                String profileImageUrl,
        @Schema(description = "닉네임", example = "포테토남") String nickname,
        @Schema(description = "기록 이미지 목록") List<DailyHistoryResponse.ImagePayload> images,
        @Schema(description = "좋아요 개수", example = "10") Long likeCount,
        @Schema(description = "좋아요 여부", example = "true") Boolean liked,
        @Schema(description = "댓글 개수", example = "5") Long commentCount,
        @Schema(description = "기록 날짜", example = "2025-01-01") LocalDate historyDate,
        @Schema(description = "상황 ID", example = "1") Long situationId,
        @Schema(description = "상황 이름", example = "데일리") String situationName,
        @Schema(description = "본문 내용", example = "오늘 날씨가 좋아서 산책을 다녀왔어요") String content,
        @Schema(description = "스타일 목록") List<DailyHistoryResponse.StylePayload> styles,
        @Schema(description = "해시태그 목록", example = "[\"데일리룩\", \"오늘의코디\"]") List<String> hashtags) {

    public static DailyHistoryResponse of(
            Long memberId,
            String profileImageUrl,
            String nickname,
            List<ImagePayload> images,
            Long likeCount,
            boolean liked,
            Long commentCount,
            LocalDate historyDate,
            Long situationId,
            String situationName,
            String content,
            List<StylePayload> styles,
            List<String> hashtags) {
        return new DailyHistoryResponse(
                memberId,
                profileImageUrl,
                nickname,
                images,
                likeCount,
                liked,
                commentCount,
                historyDate,
                situationId,
                situationName,
                content,
                styles,
                hashtags);
    }

    @Schema(name = "DailyHistoryResponseImagePayload", description = "기록 이미지 정보")
    public record ImagePayload(
            @Schema(description = "이미지 ID", example = "1") Long imageId,
            @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
                    String imageUrl) {}

    @Schema(name = "DailyHistoryResponseStylePayload", description = "스타일 정보")
    public record StylePayload(
            @Schema(description = "스타일 ID", example = "1") Long styleId,
            @Schema(description = "스타일 이름", example = "캐주얼") String styleName) {}
}
