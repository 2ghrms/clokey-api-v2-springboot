package org.clokey.domain.statistics.repository;

import java.util.List;
import java.util.Set;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.statistics.dto.CategoryCountDto;

public interface StatisticsRepositoryCustom {
    List<CategoryCountDto> countClothesByChildCategories(Long memberId, Long parentCategoryId);

    List<CategoryCountDto> countClothesByCategoriesTopN(Long memberId, int limit);

    long countClothesBySeason(Long memberId, Season season);

    Set<Long> findUtilizedClothIds(Long memberId, Season season);

    List<org.clokey.cloth.entity.Cloth> findAllClothesBySeason(Long memberId, Season season);
}
