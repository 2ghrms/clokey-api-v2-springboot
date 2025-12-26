package org.clokey.domain.coordinate.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record DailyCoordinateListResponse(
        @Schema(description = "오늘의 코디의 ID", example = "1") Long coordinateId,
        @Schema(description = "오늘의 코디 imageUrl", example = "https://example.jpg") String imageUrl,
        @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd",
                        timezone = "Asia/Seoul")
                @Schema(description = "오늘의 코디 날짜", example = "2025-01-01")
                LocalDateTime date) {}
