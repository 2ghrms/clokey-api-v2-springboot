package org.clokey.domain.cloth.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.dto.request.ClothCreateRequest;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.response.ClothCreateResponse;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.FakeAuthContext;
import org.clokey.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothServiceImpl implements ClothService {

    private final FakeAuthContext fakeAuthContext;

    private final ClothRepository clothRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ClothCreateResponse createCloths(ClothCreateRequests request) {
        final Member currentMember = fakeAuthContext.getCurrentMember();

        Map<Long, Category> categoryMap =
                getCategoryMapByIds(
                        request.content().stream()
                                .map(ClothCreateRequest::categoryId)
                                .collect(Collectors.toSet()));

        List<Cloth> cloths =
                request.content().stream()
                        .map(
                                cr -> {
                                    Category category = categoryMap.get(cr.categoryId());
                                    return Cloth.createCloth(
                                            cr.clothImageUrl(), category, currentMember);
                                })
                        .toList();

        clothRepository.saveAll(cloths);

        return ClothCreateResponse.from(cloths);
    }

    private Map<Long, Category> getCategoryMapByIds(Set<Long> ids) {
        if (categoryRepository.countByIdIn(ids) != ids.size()) {
            throw new BaseCustomException(CategoryErrorCode.CATEGORY_IN_BULK_NOT_FOUND);
        }

        return categoryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
    }
}
