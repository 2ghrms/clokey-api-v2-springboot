package org.clokey.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record MyCommentListResponse(
        @Schema(description = "기록 ID", example = "1") Long historyId,
        @Schema(description = "기록 대표 사진 url", example = "https://example.jpg") String imageUrl,
        @Schema(description = "기록 작성자 닉네임", example = "exampleNickname") String nickname,
        @Schema(description = "기록 날짜", example = "2025-01-01") LocalDate historyDate,
        @Schema(description = "기록 내용", example = "오늘의 날씨는 맑음..") String content,
        @Schema(description = "내가 작성한 댓글들") List<MyCommentListResponse.Payload> payloads) {
    @Schema(name = "MyCommentListResponsePayload", description = "내가 작성한 댓글들")
    public record Payload(
            @Schema(description = "댓글 ID", example = "1") Long commentId,
            @Schema(description = "댓글 내용", example = "이 옷 정말 예뻐요~!") String content) {}
}
