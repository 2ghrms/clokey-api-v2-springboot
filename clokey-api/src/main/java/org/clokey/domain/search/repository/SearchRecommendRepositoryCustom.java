package org.clokey.domain.search.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SearchRecommendRepositoryCustom {

    Optional<RecommendHistoryRow> findBestHistoryForUntriedStyle(
            List<Long> excludedMemberIds, Set<Long> userUsedStyleIds);

    Optional<RecommendHistoryRow> findBestHistoryForCategory(
            List<Long> excludedMemberIds, String categoryName);

    Optional<RecommendHistoryRow> findBestHistoryForHashtag(
            List<Long> excludedMemberIds, String hashtagName);

    Optional<String> findTopCategoryNameByHistoryIds(List<Long> memberHistoryIds);

    Optional<String> findMostRecentHashtagNameByMemberId(Long memberId);
}
