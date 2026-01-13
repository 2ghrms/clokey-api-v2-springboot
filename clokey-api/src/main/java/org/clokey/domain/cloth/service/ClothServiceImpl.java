package org.clokey.domain.cloth.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.category.entity.Category;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.dto.request.ClothCreateRequest;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.request.ClothImagesUploadRequest;
import org.clokey.domain.cloth.dto.request.ClothUpdateRequest;
import org.clokey.domain.cloth.dto.response.*;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.coordinate.repository.CoordinateClothRepository;
import org.clokey.domain.folder.repository.ClothFolderRepository;
import org.clokey.domain.history.repository.HistoryClothTagRepository;
import org.clokey.domain.image.event.ImageDeleteEvent;
import org.clokey.enums.ImageType;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.response.SliceResponse;
import org.clokey.util.S3Util;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cloth Service에서는 외부 AI 서버와 통신을 하는 부분이 존재하며 , Transaction(DB Connection Pool)을 주의하며 사용해야 합니다. */
@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {

    private final MemberUtil memberUtil;
    private final S3Util s3Util;

    private final ClothRepository clothRepository;
    private final CategoryRepository categoryRepository;
    private final ClothFolderRepository clothFolderRepository;
    private final HistoryClothTagRepository historyClothTagRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final CoordinateClothRepository coordinateClothRepository;

    @Override
    public ClothImagesPresignedUrlResponse getClothUploadPresignedUrls(
            ClothImagesUploadRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        // 중요 :  md5 해시로 변조 확인을 하기 때문에 들어온 순서대로 반환해야함!!
        List<String> presignedUrls =
                request.payloads().stream()
                        .map(
                                req ->
                                        s3Util.createPresignedUrl(
                                                ImageType.CLOTH_IMAGE,
                                                currentMember.getId(),
                                                req.fileExtension(),
                                                req.md5Hashes()))
                        .toList();

        return ClothImagesPresignedUrlResponse.of(presignedUrls);
    }

    @Override
    @Transactional
    public ClothCreateResponse createClothes(ClothCreateRequests request) {
        final Member currentMember = memberUtil.getCurrentMember();

        Map<Long, Category> categoryMap =
                getCategoryMapByIds(
                        request.content().stream()
                                .map(ClothCreateRequest::categoryId)
                                .collect(Collectors.toSet()));

        categoryMap.values().forEach(this::validateChildCategory);

        List<Cloth> clothes =
                request.content().stream()
                        .map(
                                cr -> {
                                    Category category = categoryMap.get(cr.categoryId());
                                    return Cloth.createCloth(
                                            cr.clothImageUrl(),
                                            cr.clothUrl(),
                                            cr.name(),
                                            cr.brand(),
                                            cr.season(),
                                            category,
                                            currentMember);
                                })
                        .toList();

        clothRepository.saveAll(clothes);

        return ClothCreateResponse.from(clothes);
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<ClothRecommendListResponse> recommendCategoryClothes(
            Long lastClothId, int size, Long categoryId, Season season) {
        final Member currentMember = memberUtil.getCurrentMember();

        Slice<ClothRecommendListResponse> result =
                clothRepository.findAllMemberRecommendClothesByCategoryAndSeason(
                        lastClothId, size, categoryId, currentMember.getId(), season);

        return SliceResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponse<ClothListResponse> getClothes(
            Long lastClothId,
            int size,
            SortDirection direction,
            Long categoryId,
            List<Season> seasons) {
        final Member currentMember = memberUtil.getCurrentMember();

        List<Long> categoryIds = resolveCategoryIds(categoryId);

        Slice<ClothListResponse> result =
                clothRepository.findAllMemberClothesByCategoriesAndSeasons(
                        lastClothId, size, direction, categoryIds, currentMember.getId(), seasons);

        return SliceResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ClothDetailsResponse getClothDetails(Long clothId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Cloth cloth = getClothById(clothId);

        validateClothOwnership(cloth, currentMember.getId());

        return ClothDetailsResponse.from(cloth);
    }

    @Override
    @Transactional
    public void updateCloth(Long clothId, ClothUpdateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Cloth cloth = getClothById(clothId);
        final Category category = getCategoryById(request.categoryId());

        validateClothOwnership(cloth, currentMember.getId());
        validateChildCategory(category);

        // 사진이 바뀌는 경우 기존 imageUrl을 기반으로 S3에서 삭제합니다.
        if (!cloth.getClothImageUrl().equals(request.clothImageUrl())) {
            eventPublisher.publishEvent(ImageDeleteEvent.of(cloth.getClothImageUrl()));
        }

        cloth.updateCloth(
                request.clothImageUrl(),
                request.clothUrl(),
                request.name(),
                request.brand(),
                request.season(),
                category);
    }

    /**
     * fyi : 옷 삭제시 옷이 속한 Coordinate의 사진에는 옷이 남아 있을 수 있습니다.
     *
     * <p>추가적으로, 이로 인해서 옷이 없는 기록, 폴더 등이 생기는 side-effect가 발생할 수 있습니다.
     */
    @Override
    @Transactional
    public void deleteCloth(Long clothId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Cloth cloth = getClothById(clothId);

        validateClothOwnership(cloth, currentMember.getId());

        coordinateClothRepository.deleteAllByClothId(cloth.getId());
        clothFolderRepository.deleteAllByClothId(cloth.getId());
        historyClothTagRepository.deleteAllByClothId(cloth.getId());

        eventPublisher.publishEvent(ImageDeleteEvent.of(cloth.getClothImageUrl()));
        clothRepository.delete(cloth);
    }

    private Map<Long, Category> getCategoryMapByIds(Set<Long> ids) {
        if (categoryRepository.countByIdIn(ids) != ids.size()) {
            throw new BaseCustomException(CategoryErrorCode.CATEGORY_IN_BULK_NOT_FOUND);
        }

        return categoryRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new BaseCustomException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }

    private Cloth getClothById(Long clothId) {
        return clothRepository
                .findById(clothId)
                .orElseThrow(() -> new BaseCustomException(ClothErrorCode.ClOTH_NOT_FOUND));
    }

    // 주어진 categoryId를 기반으로 조회용 카테고리 ID 목록을 생성합니다.
    // - categoryId가 null이면 null을 반환하여 레포지토리에서 전체 조회되도록 합니다.
    // - 1차 카테고리(부모)가 주어지면 자식 카테고리들의 ID를 반환합니다.
    // - 2차 카테고리(자식)가 주어지면 해당 ID만 반환합니다.
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

    private void validateClothOwnership(Cloth cloth, Long memberId) {
        if (!cloth.getMember().getId().equals(memberId)) {
            throw new BaseCustomException(ClothErrorCode.NOT_CLOTH_OWNER);
        }
    }

    /** 1차 카테고리(상위)로 옷을 분류할 수 없습니다. */
    private void validateChildCategory(Category category) {
        if (category.getParent() == null) {
            throw new BaseCustomException(ClothErrorCode.PARENT_CATEGORY_CLOTH);
        }
    }
}