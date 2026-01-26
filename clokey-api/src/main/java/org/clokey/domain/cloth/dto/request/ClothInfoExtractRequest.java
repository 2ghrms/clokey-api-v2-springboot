package org.clokey.domain.cloth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "옷 정보 추출 요청")
public record ClothInfoExtractRequest(
        @NotEmpty(message = "옷 이미지 URL 목록은 비워둘 수 없습니다.")
                @Size(max = 10, message = "옷 이미지 URL은 최대 10개까지 입력할 수 있습니다.")
                @Schema(
                        description = "옷 이미지 URL 목록 (최대 10개)",
                        example =
                                "[\"https://example.com/cloth1.jpg\", \"https://example.com/cloth2.jpg\"]")
                List<@NotBlank(message = "옷 이미지 URL은 비워둘 수 없습니다.") String> clothImageUrls) {}
