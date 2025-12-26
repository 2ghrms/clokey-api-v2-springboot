package org.clokey.domain.cloth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.clokey.cloth.enums.Season;

public record ClothCreateRequest(
        @NotBlank(message = "옷의 이미지 주소는 비워둘 수 없습니다.")
                @Schema(description = "옷의 이미지 주소", example = "https://example.jpg")
                String clothImageUrl,
        @Schema(description = "옷의 구매처 url (선택)", example = "https://example.com") String clothUrl,
        @Schema(description = "옷의 이름 (선택)", example = "https://example.com") String name,
        @Schema(description = "옷의 브랜드 (선택)", example = "나이키") String brand,
        @NotNull(message = "옷의 계절은 비워둘 수 없습니다.") @Schema(description = "옷의 계절", example = "SPRING")
                Season season,
        @NotNull(message = "옷의 카테고리 ID는 비워둘 수 없습니다.")
                @Schema(description = "옷의 카테고리 ID", example = "1")
                Long categoryId) {}
