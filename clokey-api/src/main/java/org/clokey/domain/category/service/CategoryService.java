package org.clokey.domain.category.service;

import java.util.List;
import org.clokey.domain.category.dto.response.GetCategoryListResponse;

public interface CategoryService {

    public List<GetCategoryListResponse> getCategoryList();
}
