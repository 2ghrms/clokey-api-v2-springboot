package org.clokey.domain.search.repository;

import java.util.List;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;
import org.clokey.domain.search.dto.response.SearchedHistoryResponse;
import org.clokey.domain.search.dto.response.SearchedMemberResponse;
import org.clokey.domain.search.enums.HistorySearchSortType;
import org.clokey.response.SliceResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
@Primary
public class NoopSearchRepository implements SearchRepository {

    @Override
    public void saveAllHistories(List<HistoryDocument> documents) {}

    @Override
    public void deleteHistory(String id) {}

    @Override
    public SliceResponse<SearchedMemberResponse> findUsersByKeyword(
            String keyword, Long page, int size, List<Long> excludedMemberIds) {
        return new SliceResponse<>(List.of(), true);
    }

    @Override
    public void saveAllMembers(List<MemberDocument> documents) {}

    @Override
    public void deleteMember(String id) {}

    @Override
    public SliceResponse<SearchedHistoryResponse> findHistoriesByKeyword(
            String keyword,
            Long page,
            int size,
            HistorySearchSortType sort,
            List<Long> excludedMemberIds) {
        return new SliceResponse<>(List.of(), true);
    }
}
