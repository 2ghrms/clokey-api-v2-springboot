package org.clokey.domain.cloth.repository;

import java.util.List;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.domain.cloth.dto.response.ClothRecommendListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface ClothRepositoryCustom {

    Slice<ClothRecommendListResponse> findAllMemberRecommendClothesByCategoryAndSeason(
            Long lastClothId, int size, List<Long> categoryIds, Long memberId, Season season);

    Slice<ClothListResponse> findAllMemberClothesByCategoriesAndSeasons(
            Long lastClothId,
            int size,
            SortDirection direction,
            List<Long> categoryId,
            Long memberId,
            List<Season> seasons);
}
