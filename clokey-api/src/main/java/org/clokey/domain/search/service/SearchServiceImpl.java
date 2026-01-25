package org.clokey.domain.search.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.category.entity.Category;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.HistoryStyleRepository;
import org.clokey.domain.history.repository.StyleRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;
import org.clokey.domain.search.dto.response.SearchedHistoryResponse;
import org.clokey.domain.search.dto.response.SearchedMemberResponse;
import org.clokey.domain.search.dto.response.SearchingRecommendResponse;
import org.clokey.domain.search.enums.HistorySearchSortType;
import org.clokey.domain.search.enums.RecommendType;
import org.clokey.domain.search.repository.RecommendHistoryRow;
import org.clokey.domain.search.repository.SearchRecommendRepositoryCustom;
import org.clokey.domain.search.repository.SearchRepository;
import org.clokey.domain.search.repository.SearchRepositoryCustom;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.Style;
import org.clokey.member.entity.Member;
import org.clokey.response.SliceResponse;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final String RECOMMEND_CACHE_KEY_PREFIX = "search:recommend:";
    private static final Duration RECOMMEND_CACHE_TTL = Duration.ofHours(24);

    private final MemberUtil memberUtil;
    private final CategoryRepository categoryRepository;
    private final SearchRepositoryCustom searchRepositoryCustom;
    private final SearchRepository searchRepository;
    private final BlockRepository blockRepository;
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final SearchDocumentService searchDocumentService;
    private final SearchRecommendRepositoryCustom searchRecommendRepository;
    private final HistoryStyleRepository historyStyleRepository;
    private final StyleRepository styleRepository;
    private final HistoryImageRepository historyImageRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<ClothListResponse> searchClothes(
            String keyword,
            Long lastClothId,
            int size,
            SortDirection direction,
            Long categoryId,
            List<Season> seasons) {
        final Member currentMember = memberUtil.getCurrentMember();

        List<Long> categoryIds = resolveCategoryIds(categoryId);

        Slice<ClothListResponse> result =
                searchRepositoryCustom.findClothesByKeyword(
                        keyword,
                        lastClothId,
                        size,
                        direction,
                        categoryIds,
                        currentMember.getId(),
                        seasons);

        return SliceResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<SearchedHistoryResponse> searchHistoryByHashtagsAndCategories(
            String keyword, Long page, Integer size, HistorySearchSortType sort) {
        Member currentMember = memberUtil.getCurrentMember();

        List<Long> excludedMemberIds =
                blockRepository.findBlockedMemberIdsByBlockerId(currentMember.getId());

        return searchRepository.findHistoriesByKeyword(
                keyword, page, size, sort, excludedMemberIds);
    }

    @Override
    public void syncAllHistories() {
        try {
            List<Long> historyIds = historyRepository.findAllIds();
            if (historyIds.isEmpty()) {
                log.info("[search] 동기화할 History가 없습니다.");
                return;
            }

            List<HistoryDocument> documents = new ArrayList<>();
            for (Long historyId : historyIds) {
                try {
                    HistoryDocument document = searchDocumentService.toHistoryDocument(historyId);
                    documents.add(document);
                } catch (Exception e) {
                    log.warn("[search] HistoryDocument 변환 실패 - historyId: {}", historyId, e);
                }
            }

            if (!documents.isEmpty()) {
                searchRepository.saveAllHistories(documents);
                log.info(
                        "[search] 전체 History 검색엔진 동기화 완료 - totalCount: {}, syncedCount: {}",
                        historyIds.size(),
                        documents.size());
            }
        } catch (Exception e) {
            log.error("[search] 전체 History 검색엔진 동기화 실패", e);
            throw new RuntimeException("전체 History 검색엔진 동기화 실패", e);
        }
    }

    @Override
    public void unSyncAllHistories() {
        try {
            List<Long> historyIds = historyRepository.findAllIds();
            if (historyIds.isEmpty()) {
                log.info("[search] 삭제할 HistoryDocument가 없습니다.");
                return;
            }

            for (Long historyId : historyIds) {
                try {
                    searchRepository.deleteHistory(historyId.toString());
                } catch (Exception e) {
                    log.warn("[search] HistoryDocument 삭제 실패 - historyId: {}", historyId, e);
                }
            }
            log.info(
                    "[search] 전체 HistoryDocument 검색엔진에서 삭제 완료 - historyCount: {}",
                    historyIds.size());
        } catch (Exception e) {
            log.error("[search] 전체 HistoryDocument 검색엔진 삭제 실패", e);
            throw new RuntimeException("전체 HistoryDocument 검색엔진 삭제 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<SearchedMemberResponse> searchUserByClokeyIdAndNickname(
            String keyword, Long page, Integer size) {
        Member currentMember = memberUtil.getCurrentMember();

        List<Long> excludedMemberIds =
                blockRepository.findBlockedMemberIdsByBlockerId(currentMember.getId());

        return searchRepository.findUsersByKeyword(keyword, page, size, excludedMemberIds);
    }

    @Override
    public void syncAllMembers() {
        try {
            List<Long> memberIds = memberRepository.findAllIds();
            if (memberIds.isEmpty()) {
                log.info("[search] 동기화할 Member가 없습니다.");
                return;
            }

            List<MemberDocument> documents = new ArrayList<>();
            for (Long memberId : memberIds) {
                try {
                    MemberDocument document = searchDocumentService.toMemberDocument(memberId);
                    documents.add(document);
                } catch (Exception e) {
                    log.warn("[search] MemberDocument 변환 실패 - memberId: {}", memberId, e);
                }
            }

            if (!documents.isEmpty()) {
                searchRepository.saveAllMembers(documents);
                log.info(
                        "[search] 전체 Member 검색엔진 동기화 완료 - totalCount: {}, syncedCount: {}",
                        memberIds.size(),
                        documents.size());
            }
        } catch (Exception e) {
            log.error("[search] 전체 Member 검색엔진 동기화 실패", e);
            throw new RuntimeException("전체 Member 검색엔진 동기화 실패", e);
        }
    }

    @Override
    public void unSyncAllMembers() {
        try {
            List<Long> memberIds = memberRepository.findAllIds();
            if (memberIds.isEmpty()) {
                log.info("[search] 삭제할 MemberDocument가 없습니다.");
                return;
            }

            for (Long memberId : memberIds) {
                try {
                    searchRepository.deleteMember(memberId.toString());
                } catch (Exception e) {
                    log.warn("[search] MemberDocument 삭제 실패 - memberId: {}", memberId, e);
                }
            }
            log.info("[search] 전체 MemberDocument 검색엔진에서 삭제 완료 - memberCount: {}", memberIds.size());
        } catch (Exception e) {
            log.error("[search] 전체 MemberDocument 검색엔진 삭제 실패", e);
            throw new RuntimeException("전체 MemberDocument 검색엔진 삭제 실패", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchingRecommendResponse> recommendInSearching() {
        Member currentMember = memberUtil.getCurrentMember();
        Long memberId = currentMember.getId();
        String cacheKey = RECOMMEND_CACHE_KEY_PREFIX + memberId;

        @SuppressWarnings("unchecked")
        List<SearchingRecommendResponse> cached =
                (List<SearchingRecommendResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (shouldRecomputeFromCache(memberId, cached)) {
                redisTemplate.delete(cacheKey);
                return computeAndCacheRecommendations(memberId, cacheKey);
            }
            return cached;
        }

        return computeAndCacheRecommendations(memberId, cacheKey);
    }

    /** 캐시된 추천 결과에 차단 유저 / 신고된 기록 / 신고된 유저가 포함되어 있으면 재조회해야 함 */
    private boolean shouldRecomputeFromCache(
            Long memberId, List<SearchingRecommendResponse> cached) {
        if (cached.isEmpty()) {
            return false;
        }
        List<Long> excludedMemberIds = blockRepository.findBlockedMemberIdsByBlockerId(memberId);
        List<Long> historyIds = cached.stream().map(SearchingRecommendResponse::historyId).toList();
        Set<Long> bannedHistoryIds =
                historyIds.isEmpty()
                        ? Set.of()
                        : historyRepository.findBannedHistoryIdsAmong(historyIds);

        Set<Long> excludedSet = new HashSet<>(excludedMemberIds);
        for (SearchingRecommendResponse r : cached) {
            if (excludedSet.contains(r.memberId()) || bannedHistoryIds.contains(r.historyId())) {
                return true;
            }
        }
        return false;
    }

    private List<SearchingRecommendResponse> computeAndCacheRecommendations(
            Long memberId, String cacheKey) {
        List<Long> excludedMemberIds = blockRepository.findBlockedMemberIdsByBlockerId(memberId);
        List<Long> userHistoryIds = historyRepository.findAllIdsByMemberId(memberId);

        Set<Long> userUsedStyleIds = new HashSet<>();
        if (!userHistoryIds.isEmpty()) {
            userUsedStyleIds =
                    historyStyleRepository.findStyleInfoByHistoryIds(userHistoryIds).stream()
                            .map(HistoryStyleRepository.HistoryStyleInfo::styleId)
                            .collect(Collectors.toSet());
        }
        Set<Long> allStyleIds =
                styleRepository.findAll().stream().map(Style::getId).collect(Collectors.toSet());
        Set<Long> untriedStyleIds = new HashSet<>(allStyleIds);
        untriedStyleIds.removeAll(userUsedStyleIds);

        String topCategoryName =
                searchRecommendRepository
                        .findTopCategoryNameByHistoryIds(userHistoryIds)
                        .orElse(null);
        String recentHashtagName =
                searchRecommendRepository
                        .findMostRecentHashtagNameByMemberId(memberId)
                        .orElse(null);

        List<SearchingRecommendResponse> results = new ArrayList<>();

        if (!untriedStyleIds.isEmpty()) {
            searchRecommendRepository
                    .findBestHistoryForUntriedStyle(excludedMemberIds, untriedStyleIds)
                    .ifPresent(
                            row ->
                                    results.add(
                                            toResponse(RecommendType.UNTRIED_STYLE, row, false)));
        }
        if (topCategoryName != null) {
            searchRecommendRepository
                    .findBestHistoryForCategory(excludedMemberIds, topCategoryName)
                    .ifPresent(
                            row ->
                                    results.add(
                                            toResponse(
                                                    RecommendType.FREQUENTLY_WORN_CATEGORY,
                                                    row,
                                                    false)));
        }
        if (recentHashtagName != null) {
            searchRecommendRepository
                    .findBestHistoryForHashtag(excludedMemberIds, recentHashtagName)
                    .ifPresent(
                            row ->
                                    results.add(
                                            toResponse(
                                                    RecommendType.RECENTLY_USED_HASHTAG,
                                                    row,
                                                    true)));
        }

        List<Long> historyIds =
                results.stream().map(SearchingRecommendResponse::historyId).toList();
        Map<Long, String> imageUrlByHistoryId = getImageUrlMap(historyIds);

        List<SearchingRecommendResponse> withImages = new ArrayList<>();
        for (SearchingRecommendResponse r : results) {
            withImages.add(
                    new SearchingRecommendResponse(
                            r.historyId(),
                            r.memberId(),
                            r.recommendType(),
                            r.title(),
                            r.subTitle(),
                            imageUrlByHistoryId.getOrDefault(r.historyId(), null)));
        }

        redisTemplate.opsForValue().set(cacheKey, withImages, RECOMMEND_CACHE_TTL);
        return withImages;
    }

    private SearchingRecommendResponse toResponse(
            RecommendType type, RecommendHistoryRow row, boolean hashtagPrefix) {
        String subTitle = hashtagPrefix ? "#" + row.subTitle() : row.subTitle();
        return new SearchingRecommendResponse(
                row.historyId(), row.memberId(), type.name(), type.getTitle(), subTitle, null);
    }

    private Map<Long, String> getImageUrlMap(List<Long> historyIds) {
        if (historyIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = historyImageRepository.getFirstImageUrlsWithHistoryId(historyIds);
        Map<Long, String> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (String) row[1]);
        }
        return map;
    }

    private List<Long> resolveCategoryIds(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        Category category = getCategoryById(categoryId);

        if (category.getParent() == null) {
            return categoryRepository.findAllByParentId(categoryId).stream()
                    .map(Category::getId)
                    .toList();
        }

        return List.of(categoryId);
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new BaseCustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }
}
