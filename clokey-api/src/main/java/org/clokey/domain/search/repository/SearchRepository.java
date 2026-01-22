package org.clokey.domain.search.repository;

import java.util.List;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;
import org.clokey.domain.search.dto.response.SearchedHistoryResponse;
import org.clokey.domain.search.dto.response.SearchedMemberResponse;
import org.clokey.domain.search.enums.HistorySearchSortType;
import org.clokey.response.SliceResponse;

public interface SearchRepository {

    // HistoryDocument 관련 메서드
    void saveAllHistories(List<HistoryDocument> documents);

    void deleteHistory(String id);

    SliceResponse<SearchedMemberResponse> findUsersByKeyword(
            String keyword, Long page, int size, List<Long> excludedMemberIds);

    // MemberDocument 관련 메서드
    void saveAllMembers(List<MemberDocument> documents);

    void deleteMember(String id);

    SliceResponse<SearchedHistoryResponse> findHistoriesByKeyword(
            String keyword,
            Long page,
            int size,
            HistorySearchSortType sort,
            List<Long> excludedMemberIds);
}
