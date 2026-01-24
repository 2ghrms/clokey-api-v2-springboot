package org.clokey.domain.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.enums.Season;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.domain.search.dto.response.SearchedHistoryResponse;
import org.clokey.domain.search.dto.response.SearchedMemberResponse;
import org.clokey.domain.search.enums.HistorySearchSortType;
import org.clokey.domain.search.service.SearchService;
import org.clokey.global.annotation.PageSize;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "14. 검색 API", description = "검색 관련 API입니다.")
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/clothes")
    @Operation(
            operationId = "Search_searchClothes",
            summary = "내 옷 목록 검색",
            description = "내 옷장의 옷 목록을 옷 이름과 브랜드명을 통해서 검색할 수 있는 API입니다.")
    public BaseResponse<SliceResponse<ClothListResponse>> searchClothes(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "이전 페이지의 옷 ID(첫 요청 시 생략)") @RequestParam(required = false)
                    Long lastClothId,
            @Parameter(description = "페이지당 조회할 옷의 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction,
            @Parameter(description = "옷의 카테고리 조건 (전체 조회시 생략)") @RequestParam(required = false)
                    Long categoryId,
            @Parameter(description = "옷의 계절 조건 (전체 조회시 생략)") @RequestParam(required = false)
                    List<Season> seasons) {
        SliceResponse<ClothListResponse> response =
                searchService.searchClothes(
                        keyword, lastClothId, size, direction, categoryId, seasons);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/histories")
    @Operation(
            operationId = "Search_searchHistoryByHashtagsAndCategories",
            summary = "기록 검색",
            description = "검색 탭에서 해시태그와 태그된 옷의 카테고리를 통해 기록을 검색할 수 있는 API입니다.")
    public BaseResponse<SliceResponse<SearchedHistoryResponse>>
            searchHistoryByHashtagsAndCategories(
                    @Parameter(description = "검색 키워드 (해시태그 or 카테고리)") @RequestParam String keyword,
                    @Parameter(description = "페이지 번호") @RequestParam Long page,
                    @Parameter(description = "페이지당 조회할 기록의 수") @RequestParam @PageSize Integer size,
                    @Parameter(description = "정렬 타입 (POPULAR: 인기순, LATEST: 최신순)")
                            @RequestParam(defaultValue = "LATEST")
                            HistorySearchSortType sort) {
        SliceResponse<SearchedHistoryResponse> response =
                searchService.searchHistoryByHashtagsAndCategories(keyword, page, size, sort);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/histories/sync-all")
    @Operation(
            operationId = "Search_syncAllHistories",
            summary = "전체 기록 검색 엔진 동기화 (개발용)",
            description = "전체 기록 검색 엔진 동기화 API입니다.")
    public BaseResponse<Void> syncAllHistories() {
        searchService.syncAllHistories();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, null);
    }

    @GetMapping("/histories/unsync-all")
    @Operation(
            operationId = "Search_unSyncAllHistories",
            summary = "전체 기록 검색 엔진 삭제 (개발용)",
            description = "전체 기록 검색 엔진 삭제 API입니다.")
    public BaseResponse<Void> unSyncAllHistories() {
        searchService.unSyncAllHistories();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, null);
    }

    @GetMapping("/members")
    @Operation(
            operationId = "Search_searchUserByClokeyIdAndNickname",
            summary = "유저 검색",
            description = "검색탭에서 클로키ID와 닉네임을 통해 유저를 검색할 수 있는 API입니다.")
    public BaseResponse<SliceResponse<SearchedMemberResponse>> searchUserByClokeyIdAndNickname(
            @Parameter(description = "검색 키워드 (클로키ID or 닉네임)") @RequestParam String keyword,
            @Parameter(description = "페이지 번호") @RequestParam Long page,
            @Parameter(description = "페이지당 조회할 유저의 수") @RequestParam @PageSize Integer size) {
        SliceResponse<SearchedMemberResponse> response =
                searchService.searchUserByClokeyIdAndNickname(keyword, page, size);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/members/sync-all")
    @Operation(
            operationId = "Search_syncAllMembers",
            summary = "전체 유저 검색 엔진 동기화 (개발용)",
            description = "전체 유저 검색 엔진 동기화 API입니다.")
    public BaseResponse<Void> syncAllMembers() {
        searchService.syncAllMembers();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, null);
    }

    @GetMapping("/members/unsync-all")
    @Operation(
            operationId = "Search_unSyncAllMembers",
            summary = "전체 유저 검색 엔진 삭제 (개발용)",
            description = "전체 유저 검색 엔진 삭제 API입니다.")
    public BaseResponse<Void> unSyncAllMembers() {
        searchService.unSyncAllMembers();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, null);
    }

    /* :TODO: 검색 탭 추천 API 추후 구현
    @GetMapping("/recommendations")
    @Operation(
            operationId = "Search_recommendInSearching",
            summary = "검색 탭 기록 추천",
            description = "검색탭에서 사용자가 시도하지 않은 스타일별/자주 착용한 카테고리/최근 태그한 해시태그별 1개씩의 기록울 추천하는 API입니다.")
    public BaseResponse<List<SearchingRecommendResponse>> recommendInSearching() {
        List<SearchingRecommendResponse> response =
                searchService.recommendInSearching();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
    */
}
