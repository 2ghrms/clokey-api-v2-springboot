package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record HistoryImagesPresignedUrlResponse(
        @Schema(description = "업로드용 presigned url 리스트") List<String> urls,
        @Schema(description = "저장/조회용 공개 객체 url 리스트") List<String> objectUrls) {
    public static HistoryImagesPresignedUrlResponse of(List<String> urls, List<String> objectUrls) {
        return new HistoryImagesPresignedUrlResponse(urls, objectUrls);
    }
}
