package org.clokey.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReplyListResponse(
        @Schema(description = "대댓글 ID", example = "1") Long replyId,
        @Schema(description = "댓글 작성자 ID", example = "1") Long memberId,
        @Schema(description = "댓글 작성자 닉네임", example = "포테토남") String nickname,
        @Schema(description = "댓글 작성자 이미지 주소", example = "https://s3.amazonaws.com/example.jpg")
                String profileImageUrl,
        @Schema(description = "댓글 내용", example = "이 옷 정보 알려주세요!") String content,
        @Schema(description = "내가 작성한 대댓글인가?", example = "false") boolean isMine,
        @Schema(description = "댓글 삭제 가능 여부", example = "false") boolean canDelete) {}
