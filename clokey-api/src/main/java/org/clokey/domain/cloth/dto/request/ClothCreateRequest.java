package org.clokey.domain.cloth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClothCreateRequest(
        @NotBlank(message = "옷의 이미지 주소는 비워둘 수 없습니다.")
                @Schema(description = "옷의 이미지 주소", example = "https://example.jpg")
                String clothImageUrl,
        @NotNull(message = "옷의 카테고리 ID는 비워둘 수 없습니다.")
                @Schema(description = "옷의 카테고리 ID", example = "1")
                Long categoryId) {}
