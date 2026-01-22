package org.clokey.domain.search.repository;

import io.vanslog.spring.data.meilisearch.core.MeilisearchOperations;
import io.vanslog.spring.data.meilisearch.core.SearchHit;
import io.vanslog.spring.data.meilisearch.core.SearchHits;
import io.vanslog.spring.data.meilisearch.core.query.IndexQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;
import org.clokey.domain.search.dto.response.SearchedHistoryResponse;
import org.clokey.domain.search.dto.response.SearchedMemberResponse;
import org.clokey.domain.search.enums.HistorySearchSortType;
import org.clokey.response.SliceResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MeiliSearchRepositoryImpl implements SearchRepository {

    private static final String HISTORY_INDEX = "histories";
    private static final String MEMBER_INDEX = "members";
    private final MeilisearchOperations meilisearchOperations;
    private final MeilisearchHistoryRepository historyRepository;
    private final MeilisearchMemberRepository memberRepository;

    // MemberDocument 관련 메서드 구현
    @Override
    public void saveAllMembers(List<MemberDocument> documents) {
        memberRepository.saveAll(documents);
    }

    @Override
    public void deleteMember(String id) {
        memberRepository.deleteById(id);
    }

    @Override
    public SliceResponse<SearchedMemberResponse> findUsersByKeyword(
            String keyword, Long page, int size, List<Long> excludedMemberIds) {

        try {
            // 1. 필터 조건 구성 (신고당한 유저 + 차단한 유저 제외)
            List<String> filterConditions = new ArrayList<>();
            filterConditions.add("memberStatus != \"BANNED\"");

            if (excludedMemberIds != null && !excludedMemberIds.isEmpty()) {
                String memberIdFilter =
                        excludedMemberIds.stream()
                                .map(String::valueOf)
                                .map(v -> "\"" + v + "\"")
                                .collect(Collectors.joining(", "));
                filterConditions.add("id NOT IN [" + memberIdFilter + "]");
            }

            // 2. IndexQuery 생성 (size+1 조회로 다음 페이지 존재 여부 판단)
            IndexQuery indexQuery =
                    IndexQuery.builder()
                            .withIndexUid(MEMBER_INDEX)
                            .withQ(keyword)
                            .withFilter(filterConditions.toArray(new String[0]))
                            .withPageable(PageRequest.of(page.intValue(), size + 1))
                            .build();

            // 3. 검색 실행
            SearchHits<MemberDocument> result =
                    meilisearchOperations.search(indexQuery, MemberDocument.class);
            List<SearchHit<MemberDocument>> hits = result.getSearchHits();

            boolean hasNext = hits.size() > size;
            List<SearchedMemberResponse> content =
                    hits.stream()
                            .limit(size)
                            .map(
                                    hit -> {
                                        MemberDocument document = hit.getContent();
                                        return new SearchedMemberResponse(
                                                Long.valueOf(document.getId()),
                                                document.getProfileImageUrl(),
                                                document.getNickname(),
                                                document.getClokeyId());
                                    })
                            .collect(Collectors.toList());

            return new SliceResponse<>(content, !hasNext);
        } catch (Exception e) {
            throw new RuntimeException("MeiliSearch search failed", e);
        }
    }

    // HistoryDocument 관련 메서드 구현
    @Override
    public void saveAllHistories(List<HistoryDocument> documents) {
        historyRepository.saveAll(documents);
    }

    @Override
    public void deleteHistory(String id) {
        historyRepository.deleteById(id);
    }

    @Override
    public SliceResponse<SearchedHistoryResponse> findHistoriesByKeyword(
            String keyword,
            Long page,
            int size,
            HistorySearchSortType sort,
            List<Long> excludedMemberIds) {

        try {
            // 1. 필터 조건 구성 (신고당한 기록 + 차단한 유저의 기록 제외)
            List<String> filterConditions = new ArrayList<>();
            filterConditions.add("banned = false");

            if (excludedMemberIds != null && !excludedMemberIds.isEmpty()) {
                String memberIdFilter =
                        excludedMemberIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "));
                filterConditions.add("memberId NOT IN [" + memberIdFilter + "]");
            }

            // 2. 정렬 설정
            Sort sortOption =
                    (sort == HistorySearchSortType.POPULAR)
                            ? Sort.by(Sort.Order.desc("likeCount"))
                            : Sort.by(Sort.Order.desc("createdAt"));

            // 3. IndexQuery 생성
            IndexQuery indexQuery =
                    IndexQuery.builder()
                            .withIndexUid(HISTORY_INDEX)
                            .withQ(keyword)
                            .withFilter(filterConditions.toArray(new String[0]))
                            .withSort(sortOption)
                            .withPageable(PageRequest.of(page.intValue(), size + 1))
                            .build();

            // 4. 검색 실행
            SearchHits<HistoryDocument> searchHits =
                    meilisearchOperations.search(indexQuery, HistoryDocument.class);
            List<SearchHit<HistoryDocument>> searchHitsList = searchHits.getSearchHits();

            // 5. DTO 매핑
            boolean hasNext = searchHitsList.size() > size;
            List<SearchedHistoryResponse> content =
                    searchHitsList.stream()
                            .limit(size)
                            .map(
                                    hit -> {
                                        HistoryDocument document = hit.getContent();
                                        return new SearchedHistoryResponse(
                                                Long.valueOf(document.getId()),
                                                document.getHistoryImageUrl(),
                                                document.getProfileImageUrl(),
                                                document.getNickname());
                                    })
                            .collect(Collectors.toList());

            return new SliceResponse<>(content, !hasNext);
        } catch (Exception e) {
            throw new RuntimeException("MeiliSearch search failed", e);
        }
    }
}
