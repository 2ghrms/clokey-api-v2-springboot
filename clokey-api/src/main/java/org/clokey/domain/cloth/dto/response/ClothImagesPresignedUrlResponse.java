package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ClothImagesPresignedUrlResponse(
        @Schema(description = "생성된 presigned url 리스트") List<String> urls) {
    public static ClothImagesPresignedUrlResponse of(List<String> urls) {
        return new ClothImagesPresignedUrlResponse(urls);
    }
}
