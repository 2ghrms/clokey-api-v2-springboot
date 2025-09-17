package org.clokey.domain.cloth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.clokey.cloth.entity.Cloth;

public record ClothCreateResponse(
        @Schema(description = "생성된 옷 ID들", example = "[1,2,3,4]") List<Long> clothIds) {
    public static ClothCreateResponse from(List<Cloth> clothes) {
        return new ClothCreateResponse(clothes.stream().map(Cloth::getId).toList());
    }
}
