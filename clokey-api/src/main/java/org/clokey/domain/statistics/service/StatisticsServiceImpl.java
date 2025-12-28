package org.clokey.domain.statistics.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.statistics.dto.CategoryCountDto;
import org.clokey.domain.statistics.dto.response.ClosetUtilizationResponse;
import org.clokey.domain.statistics.dto.response.FavoriteCategoryItemsResponse;
import org.clokey.domain.statistics.dto.response.FavoriteItemsResponse;
import org.clokey.domain.statistics.dto.response.StatisticsCheckConditionResponse;
import org.clokey.domain.statistics.exception.StatisticsErrorCode;
import org.clokey.domain.statistics.repository.StatisticsRepositoryCustom;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final MemberUtil memberUtil;
    private final CategoryRepository categoryRepository;
    private final StatisticsRepositoryCustom statisticsRepositoryCustom;
    private final HistoryRepository historyRepository;
    private final ClothRepository clothRepository;
    private final CoordinateRepository coordinateRepository;

    @Override
    public StatisticsCheckConditionResponse checkStatisticsCondition() {
        final Member currentMember = memberUtil.getCurrentMember();

        boolean hasHistory = historyRepository.existsByMemberId(currentMember.getId());
        boolean hasCloth = clothRepository.existsByMemberId(currentMember.getId());
        boolean hasCoordinate = coordinateRepository.existsByMemberId(currentMember.getId());

        boolean canAggregate = hasHistory && hasCloth && hasCoordinate;

        return StatisticsCheckConditionResponse.of(canAggregate);
    }

    @Override
    public FavoriteCategoryItemsResponse getFavoriteCategoryItems(Long categoryId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Category parentCategory = getCategoryById(categoryId);

        validateParentCategory(parentCategory);

        List<CategoryCountDto> categoryCounts =
                statisticsRepositoryCustom.countClothesByChildCategories(
                        currentMember.getId(), parentCategory.getId());

        List<FavoriteCategoryItemsResponse.Payload> payloads =
                buildFavoriteCategoryItemsPayloads(categoryCounts);

        return FavoriteCategoryItemsResponse.of(payloads);
    }

    @Override
    public FavoriteItemsResponse getFavoriteItems() {
        final Member currentMember = memberUtil.getCurrentMember();

        List<CategoryCountDto> categoryCounts =
                statisticsRepositoryCustom.countClothesByCategoriesTopN(currentMember.getId(), 5);

        List<FavoriteItemsResponse.Payload> payloads =
                categoryCounts.stream()
                        .map(
                                dto ->
                                        new FavoriteItemsResponse.Payload(
                                                dto.categoryId(), dto.categoryName(), dto.count()))
                        .toList();

        return FavoriteItemsResponse.of(payloads);
    }

    @Override
    public ClosetUtilizationResponse getClosetUtilization(Season season) {
        final Member currentMember = memberUtil.getCurrentMember();

        List<Cloth> allClothes =
                statisticsRepositoryCustom.findAllClothesBySeason(currentMember.getId(), season);

        Set<Long> utilizedClothIds =
                statisticsRepositoryCustom.findUtilizedClothIds(currentMember.getId(), season);

        List<ClosetUtilizationResponse.Payload> utilizedClothes =
                allClothes.stream()
                        .filter(cloth -> utilizedClothIds.contains(cloth.getId()))
                        .map(
                                cloth ->
                                        new ClosetUtilizationResponse.Payload(
                                                cloth.getClothImageUrl(),
                                                cloth.getName(),
                                                cloth.getBrand()))
                        .toList();

        List<ClosetUtilizationResponse.Payload> unutilizedClothes =
                allClothes.stream()
                        .filter(cloth -> !utilizedClothIds.contains(cloth.getId()))
                        .map(
                                cloth ->
                                        new ClosetUtilizationResponse.Payload(
                                                cloth.getClothImageUrl(),
                                                cloth.getName(),
                                                cloth.getBrand()))
                        .toList();

        long utilizedCount = utilizedClothes.size();
        long unutilizedCount = unutilizedClothes.size();

        return ClosetUtilizationResponse.of(
                utilizedCount, unutilizedCount, utilizedClothes, unutilizedClothes);
    }

    /** 4개 이하의 2차 카테고리들이 집계되었으면 모두 보여주고 5개 이상 부터는 탑3를 제외한 나머지는 기타로 묶이게 됩니다. */
    private List<FavoriteCategoryItemsResponse.Payload> buildFavoriteCategoryItemsPayloads(
            List<CategoryCountDto> categoryCounts) {
        if (categoryCounts.isEmpty()) {
            return List.of();
        }

        long totalCount = categoryCounts.stream().mapToLong(CategoryCountDto::count).sum();

        if (categoryCounts.size() <= 4) {
            return categoryCounts.stream()
                    .map(
                            dto ->
                                    new FavoriteCategoryItemsResponse.Payload(
                                            dto.categoryId(),
                                            dto.categoryName(),
                                            calculateOccupancyRate(dto.count(), totalCount),
                                            dto.count()))
                    .toList();
        }

        List<FavoriteCategoryItemsResponse.Payload> payloads = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            CategoryCountDto dto = categoryCounts.get(i);
            payloads.add(
                    new FavoriteCategoryItemsResponse.Payload(
                            dto.categoryId(),
                            dto.categoryName(),
                            calculateOccupancyRate(dto.count(), totalCount),
                            dto.count()));
        }

        long otherCount = categoryCounts.stream().skip(3).mapToLong(CategoryCountDto::count).sum();
        payloads.add(
                new FavoriteCategoryItemsResponse.Payload(
                        null, "기타", calculateOccupancyRate(otherCount, totalCount), otherCount));

        return payloads;
    }

    private Double calculateOccupancyRate(long count, long totalCount) {
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) count / totalCount;
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new BaseCustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    private void validateParentCategory(Category category) {
        if (category.getParent() != null) {
            throw new BaseCustomException(StatisticsErrorCode.NOT_PARENT_CATEGORY);
        }
    }
}
