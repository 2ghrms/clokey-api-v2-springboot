package org.clokey.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReplyCreateRequest(
        @NotBlank(message = "대댓글 내용을 비워둘 수 없습니다.")
                @Size(max = 100, message = "대댓글의 내용은 최대 100자까지 가능합니다.")
                @Schema(description = "대댓글의 내용", example = "이 옷 정보좀 알 수 있을까요!?")
                String content) {}
