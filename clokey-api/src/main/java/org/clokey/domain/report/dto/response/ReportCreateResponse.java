package org.clokey.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.report.entity.Report;

public record ReportCreateResponse(
        @Schema(description = "생성된 신고의 Id값", example = "1") Long reportId) {
    public static ReportCreateResponse from(Report report) {
        return new ReportCreateResponse(report.getId());
    }
}
