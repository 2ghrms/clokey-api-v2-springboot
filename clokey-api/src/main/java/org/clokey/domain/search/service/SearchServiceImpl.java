package org.clokey.domain.search.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.category.entity.Category;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.search.document.HistoryDocument;
import org.clokey.domain.search.document.MemberDocument;
import org.clokey.domain.search.dto.response.SearchedHistoryResponse;
import org.clokey.domain.search.dto.response.SearchedMemberResponse;
import org.clokey.domain.search.enums.HistorySearchSortType;
import org.clokey.domain.search.repository.SearchRepository;
import org.clokey.domain.search.repository.SearchRepositoryCustom;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.response.SliceResponse;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final MemberUtil memberUtil;
    private final CategoryRepository categoryRepository;
    private final SearchRepositoryCustom searchRepositoryCustom;
    private final SearchRepository searchRepository;
    private final BlockRepository blockRepository;
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final SearchDocumentService searchDocumentService;

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

    /* :TODO 추천 메소드 구현
    @Override
    @Transactional(readOnly = true)
    List<SearchingRecommendResponse> recommendInSearching() {

    }
    */

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
