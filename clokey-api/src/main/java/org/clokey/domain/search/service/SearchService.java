package org.clokey.domain.search.service;

import java.util.List;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.domain.search.dto.response.SearchedHistoryResponse;
import org.clokey.domain.search.dto.response.SearchedMemberResponse;
import org.clokey.domain.search.enums.HistorySearchSortType;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface SearchService {

    SliceResponse<ClothListResponse> searchClothes(
            String keyword,
            Long lastClothId,
            int size,
            SortDirection direction,
            Long categoryId,
            List<Season> seasons);

    void syncAllHistories();

    void unSyncAllHistories();

    SliceResponse<SearchedHistoryResponse> searchHistoryByHashtagsAndCategories(
            String keyword, Long page, Integer size, HistorySearchSortType sort);

    SliceResponse<SearchedMemberResponse> searchUserByClokeyIdAndNickname(
            String keyword, Long page, Integer size);

    // TODO : 추천 검색어 기능 구현 후 주석 해제
    //    List<SearchingRecommendResponse> recommendInSearching();
}
