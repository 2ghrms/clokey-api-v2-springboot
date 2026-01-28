package org.clokey.domain.cloth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "사진에서 옷 탐지 요청")
public record ClothDetectRequest(
        @NotBlank(message = "이미지 URL은 비워둘 수 없습니다.")
                @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
                String imageUrl) {}
