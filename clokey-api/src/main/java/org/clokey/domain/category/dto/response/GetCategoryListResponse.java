package org.clokey.domain.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.clokey.category.entity.Category;

public record GetCategoryListResponse(
        @Schema(description = "카테고리 ID", example = "1") Long id,
        @Schema(description = "카테고리 이름", example = "상의") String name,
        @Schema(description = "하위 카테고리 목록") List<GetCategoryListResponse> children) {
    public static GetCategoryListResponse from(Category category, List<Category> subCategories) {
        List<GetCategoryListResponse> chlildResponses =
                subCategories.stream()
                        .map(
                                sub ->
                                        new GetCategoryListResponse(
                                                sub.getId(), sub.getName(), List.of()))
                        .toList();
        return new GetCategoryListResponse(category.getId(), category.getName(), chlildResponses);
    }
}
