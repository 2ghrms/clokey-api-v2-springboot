package org.clokey.domain.search.repository;

import java.util.List;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface SearchRepositoryCustom {

    Slice<ClothListResponse> findClothesByKeyword(
            String keyword,
            Long lastClothId,
            int size,
            SortDirection direction,
            List<Long> categoryIds,
            Long memberId,
            List<Season> seasons);
}
