package org.clokey.domain.history.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record HistoryImagesPresignedUrlResponse(
        @Schema(description = "생성된 presigned url 리스트") List<String> urls) {
    public static HistoryImagesPresignedUrlResponse of(List<String> urls) {
        return new HistoryImagesPresignedUrlResponse(urls);
    }
}
