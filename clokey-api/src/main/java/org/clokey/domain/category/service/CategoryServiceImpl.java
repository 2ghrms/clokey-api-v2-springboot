package org.clokey.domain.category.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.category.entity.Category;
import org.clokey.domain.category.dto.response.GetCategoryListResponse;
import org.clokey.domain.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public List<GetCategoryListResponse> getCategoryList() {

        final List<Category> parentCategories = categoryRepository.findAllByParentIsNull();

        return parentCategories.stream()
                .map(
                        parent -> {
                            List<Category> childCategories =
                                    categoryRepository.findAllByParentId(parent.getId());
                            return GetCategoryListResponse.from(parent, childCategories);
                        })
                .toList();
    }
}
