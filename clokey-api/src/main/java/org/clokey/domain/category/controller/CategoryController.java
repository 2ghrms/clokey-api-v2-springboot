package org.clokey.domain.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.category.dto.response.GetCategoryListResponse;
import org.clokey.domain.category.service.CategoryService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "02. 카테고리 API", description = "카테고리 관련 API입니다.")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            operationId = "Category_getAllCategories",
            summary = "카테고리 목록 조회",
            description = "카테고리 목록을 조회합니다.")
    public BaseResponse<List<GetCategoryListResponse>> getAllCategories() {
        List<GetCategoryListResponse> categories = categoryService.getCategoryList();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, categories);
    }
}
