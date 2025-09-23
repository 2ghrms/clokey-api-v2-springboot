package org.clokey.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.clokey.global.annotation.Enum;
import org.clokey.report.enums.ReportReason;
import org.clokey.report.enums.TargetType;

public record ReportCreateRequest(
        @NotNull(message = "신고 컨텐츠 ID는 비워둘 수 없습니다.")
                @Schema(description = "신고 컨텐츠 ID", example = "1")
                Long targetId,
        @Enum(message = "신고 컨텐츠 타입은 비워둘 수 없으며 COMMENT/REPLY/HISTORY 존재")
                @Schema(description = "신고 컨텐츠의 타입", example = "COMMENT")
                TargetType targetType,
        @Enum(message = "신고 사유는 비워둘 수 없습니다.")
                @Schema(description = "신고 사유", example = "SWEARING_AND_CURSING")
                ReportReason reportReason,
        @Schema(description = "상세 사유 입력/ 빈칸 가능", example = "댓글에 욕설이 가득해요..ㅠㅠ") String content) {}
