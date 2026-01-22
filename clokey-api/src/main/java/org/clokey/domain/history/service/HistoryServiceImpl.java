package org.clokey.domain.history.service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.request.HistoryImagesUploadRequest;
import org.clokey.domain.history.dto.request.HistoryUpdateRequest;
import org.clokey.domain.history.dto.response.DailyHistoryResponse;
import org.clokey.domain.history.dto.response.HistoryClothTagListResponse;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.dto.response.HistoryImagesPresignedUrlResponse;
import org.clokey.domain.history.dto.response.HistoryOwnershipCheckResponse;
import org.clokey.domain.history.dto.response.MonthlyHistoryResponse;
import org.clokey.domain.history.dto.response.SituationListResponse;
import org.clokey.domain.history.dto.response.StyleListResponse;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.domain.history.repository.*;
import org.clokey.domain.image.event.ImagesDeleteEvent;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.report.repository.ReportRepository;
import org.clokey.domain.search.event.HistoryDeleteEvent;
import org.clokey.domain.search.event.MeiliSearchSyncEvent;
import org.clokey.enums.ImageType;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.*;
import org.clokey.member.entity.Member;
import org.clokey.member.enums.Visibility;
import org.clokey.report.enums.TargetType;
import org.clokey.util.S3Util;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoryServiceImpl implements HistoryService {

    private final MemberUtil memberUtil;

    private final HistoryRepository historyRepository;
    private final ClothRepository clothRepository;
    private final HistoryHashtagRepository historyHashtagRepository;
    private final HashtagRepository hashtagRepository;
    private final HistoryStyleRepository historyStyleRepository;
    private final StyleRepository styleRepository;
    private final SituationRepository situationRepository;
    private final HistoryImageRepository historyImageRepository;
    private final HistoryClothTagRepository historyClothTagRepository;
    private final MemberLikeRepository memberLikeRepository;
    private final CommentRepository commentRepository;
    private final BlockRepository blockRepository;
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final S3Util s3Util;

    @Override
    @Transactional
    public HistoryCreateResponse createHistory(HistoryCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        final Situation situation = getSituationById(request.situationId());

        final List<Long> styleIds = request.styleIds();
        final Map<Long, Style> styleMap = validateAndLoadStyles(styleIds);

        final List<Long> allRequestedClothIds =
                request.payloads().stream()
                        .filter(p -> p.clothTags() != null)
                        .flatMap(p -> p.clothTags().stream())
                        .map(HistoryCreateRequest.ClothTag::clothId)
                        .toList();

        final Map<Long, Cloth> clothMap =
                validateAndLoadClothes(currentMember, allRequestedClothIds);

        final String content =
                Optional.ofNullable(request.content()).map(String::trim).orElse(null);
        final History history =
                History.createHistory(LocalDate.now(), content, currentMember, situation);
        historyRepository.save(history);

        List<HistoryImage> images = new ArrayList<>();
        List<HistoryClothTag> clothTags = new ArrayList<>();

        for (HistoryCreateRequest.Payload payload : request.payloads()) {
            final HistoryImage historyImage =
                    HistoryImage.createHistoryImage(payload.imageUrl(), history);
            images.add(historyImage);

            if (payload.clothTags() != null && !payload.clothTags().isEmpty()) {
                for (HistoryCreateRequest.ClothTag clothTag : payload.clothTags()) {
                    final Cloth cloth = clothMap.get(clothTag.clothId());
                    final HistoryClothTag historyClothTag =
                            HistoryClothTag.createHistoryClothTag(
                                    historyImage,
                                    cloth,
                                    clothTag.locationX(),
                                    clothTag.locationY());
                    clothTags.add(historyClothTag);
                }
            }
        }
        saveHistoryRelations(history, styleIds, styleMap, images, clothTags, request.hashtags());

        eventPublisher.publishEvent(
                MeiliSearchSyncEvent.of(MeiliSearchSyncEvent.EntityType.HISTORY, history.getId()));

        return new HistoryCreateResponse(history.getId());
    }

    @Override
    @Transactional
    public void updateHistory(Long historyId, HistoryUpdateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(historyId);

        validateHistoryOwner(history, currentMember.getId());

        final Situation situation = getSituationById(request.situationId());

        final List<Long> styleIds = request.styleIds();
        final Map<Long, Style> styleMap = validateAndLoadStyles(styleIds);

        final List<Long> allRequestedClothIds =
                request.payloads().stream()
                        .filter(p -> p.clothTags() != null)
                        .flatMap(p -> p.clothTags().stream())
                        .map(HistoryUpdateRequest.ClothTag::clothId)
                        .toList();

        final Map<Long, Cloth> clothMap =
                validateAndLoadClothes(currentMember, allRequestedClothIds);

        final String content =
                Optional.ofNullable(request.content()).map(String::trim).orElse(null);

        history.updateHistory(content, situation);

        clearStylesAndHashtags(history.getId());

        List<HistoryImage> existingImages = historyImageRepository.findByHistoryId(historyId);
        Map<String, HistoryImage> existingImageMap =
                existingImages.stream()
                        .collect(Collectors.toMap(HistoryImage::getImageUrl, Function.identity()));

        Set<String> requestedImageUrls =
                request.payloads().stream()
                        .map(HistoryUpdateRequest.Payload::imageUrl)
                        .collect(Collectors.toSet());

        List<HistoryImage> imagesToDelete =
                existingImages.stream()
                        .filter(image -> !requestedImageUrls.contains(image.getImageUrl()))
                        .toList();

        if (!imagesToDelete.isEmpty()) {
            deleteClothTagsByHistoryImageIds(
                    imagesToDelete.stream().map(HistoryImage::getId).toList());
            historyImageRepository.deleteAllInBatch(imagesToDelete);
        }

        Set<Long> keptImageIds =
                existingImages.stream()
                        .filter(image -> requestedImageUrls.contains(image.getImageUrl()))
                        .map(HistoryImage::getId)
                        .collect(Collectors.toSet());

        if (!keptImageIds.isEmpty()) {
            deleteClothTagsByHistoryImageIds(keptImageIds.stream().toList());
        }

        List<HistoryImage> newImages = new ArrayList<>();
        List<HistoryClothTag> clothTags = new ArrayList<>();

        for (HistoryUpdateRequest.Payload payload : request.payloads()) {
            final HistoryImage historyImage =
                    existingImageMap.getOrDefault(
                            payload.imageUrl(),
                            HistoryImage.createHistoryImage(payload.imageUrl(), history));

            if (!existingImageMap.containsKey(payload.imageUrl())) {
                newImages.add(historyImage);
            }

            if (payload.clothTags() != null && !payload.clothTags().isEmpty()) {
                for (HistoryUpdateRequest.ClothTag clothTag : payload.clothTags()) {
                    final Cloth cloth = clothMap.get(clothTag.clothId());
                    final HistoryClothTag historyClothTag =
                            HistoryClothTag.createHistoryClothTag(
                                    historyImage,
                                    cloth,
                                    clothTag.locationX(),
                                    clothTag.locationY());
                    clothTags.add(historyClothTag);
                }
            }
        }
        saveHistoryRelations(history, styleIds, styleMap, newImages, clothTags, request.hashtags());

        eventPublisher.publishEvent(
                MeiliSearchSyncEvent.of(MeiliSearchSyncEvent.EntityType.HISTORY, historyId));
    }

    @Override
    public StyleListResponse getAllStyles() {
        return styleRepository.findAllStyles();
    }

    @Override
    public SituationListResponse getAllSituations() {
        return situationRepository.findAllSituations();
    }

    @Override
    public DailyHistoryResponse getDailyHistory(Long historyId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(historyId);

        validateBlockedAccess(currentMember.getId(), history);

        List<DailyHistoryResponse.ImagePayload> images =
                history.getHistoryImages().stream()
                        .map(
                                image ->
                                        new DailyHistoryResponse.ImagePayload(
                                                image.getId(), image.getImageUrl()))
                        .toList();

        long likeCount = memberLikeRepository.countByHistoryId(historyId);

        long commentCount = commentRepository.countByHistoryIdAndBannedFalse(historyId);

        List<DailyHistoryResponse.StylePayload> styles =
                history.getHistoryStyles().stream()
                        .map(
                                historyStyle ->
                                        new DailyHistoryResponse.StylePayload(
                                                historyStyle.getStyle().getId(),
                                                historyStyle.getStyle().getName()))
                        .toList();

        return DailyHistoryResponse.of(
                history.getMember().getId(),
                history.getMember().getProfileImageUrl(),
                history.getMember().getNickname(),
                images,
                likeCount,
                commentCount,
                history.getHistoryDate(),
                history.getSituation().getId(),
                history.getSituation().getName(),
                styles);
    }

    @Override
    public HistoryClothTagListResponse getHistoryClothTags(Long historyImageId) {
        getHistoryImageById(historyImageId);

        List<HistoryClothTag> historyClothTags =
                historyClothTagRepository.findAllByHistoryImageIdWithCloth(historyImageId);

        List<HistoryClothTagListResponse.Payload> payloads =
                historyClothTags.stream()
                        .map(
                                tag ->
                                        new HistoryClothTagListResponse.Payload(
                                                tag.getId(),
                                                tag.getCloth().getId(),
                                                tag.getCloth().getClothImageUrl(),
                                                tag.getCloth().getName(),
                                                tag.getCloth().getBrand(),
                                                tag.getLocation().getLocationX(),
                                                tag.getLocation().getLocationY()))
                        .toList();

        return HistoryClothTagListResponse.of(payloads);
    }

    @Override
    public MonthlyHistoryResponse getMonthlyHistory(Long memberId, int year, int month) {
        final Member currentMember = memberUtil.getCurrentMember();
        final Member targetMember = getMemberById(memberId);

        validateMemberAccess(currentMember.getId(), targetMember);

        List<History> histories =
                historyRepository.findByMemberIdAndYearAndMonthNotBanned(memberId, year, month);

        List<MonthlyHistoryResponse.Payload> payloads =
                histories.stream()
                        .map(
                                history -> {
                                    String firstImageUrl =
                                            history.getHistoryImages().isEmpty()
                                                    ? null
                                                    : history.getHistoryImages()
                                                            .get(0)
                                                            .getImageUrl();
                                    return new MonthlyHistoryResponse.Payload(
                                            history.getId(), firstImageUrl);
                                })
                        .toList();

        return MonthlyHistoryResponse.of(payloads);
    }

    @Override
    public HistoryOwnershipCheckResponse checkHistoryOwnership(Long historyId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(historyId);

        boolean isOwner = history.getMember().getId().equals(currentMember.getId());

        return HistoryOwnershipCheckResponse.of(isOwner);
    }

    @Override
    @Transactional
    public void deleteHistory(Long historyId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history = getHistoryById(historyId);

        validateHistoryOwner(history, currentMember.getId());

        List<HistoryImage> images = historyImageRepository.findByHistoryId(historyId);
        List<Long> imageIds = images.stream().map(HistoryImage::getId).toList();

        if (!imageIds.isEmpty()) {
            deleteClothTagsByHistoryImageIds(imageIds);
            historyImageRepository.deleteAll(images);
        }

        clearStylesAndHashtags(historyId);
        memberLikeRepository.deleteAllByHistoryId(historyId);
        commentRepository.deleteAllByHistoryId(historyId);
        reportRepository.deleteAllByTargetTypeAndTargetId(TargetType.HISTORY, historyId);

        historyRepository.delete(history);

        eventPublisher.publishEvent(HistoryDeleteEvent.of(historyId));

        List<String> imageUrls =
                images.stream().map(HistoryImage::getImageUrl).filter(Objects::nonNull).toList();
        if (!imageUrls.isEmpty()) {
            eventPublisher.publishEvent(ImagesDeleteEvent.of(imageUrls));
        }
    }

    @Override
    public HistoryImagesPresignedUrlResponse getHistoryUploadPresignedUrls(
            HistoryImagesUploadRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        List<String> presignedUrls =
                request.payloads().stream()
                        .map(
                                payload ->
                                        s3Util.createPresignedUrl(
                                                ImageType.HISTORY_IMAGE,
                                                currentMember.getId(),
                                                payload.fileExtension(),
                                                payload.md5Hashes()))
                        .toList();

        return HistoryImagesPresignedUrlResponse.of(presignedUrls);
    }

    private HistoryImage getHistoryImageById(Long historyImageId) {
        return historyImageRepository
                .findById(historyImageId)
                .orElseThrow(
                        () -> new BaseCustomException(HistoryErrorCode.HISTORY_IMAGE_NOT_FOUND));
    }

    private void saveHistoryRelations(
            History history,
            List<Long> styleIds,
            Map<Long, Style> styleMap,
            List<HistoryImage> images,
            List<HistoryClothTag> clothTags,
            List<String> hashtags) {
        saveImagesAndClothTags(images, clothTags);
        saveHistoryStyles(history, styleIds, styleMap);
        saveHistoryHashtags(history, hashtags);
    }

    private void saveImagesAndClothTags(
            List<HistoryImage> images, List<HistoryClothTag> clothTags) {
        if (!images.isEmpty()) {
            historyImageRepository.saveAll(images);
        }
        if (!clothTags.isEmpty()) {
            historyClothTagRepository.bulkInsertHistoryClothTags(clothTags);
        }
    }

    private void saveHistoryStyles(
            History history, List<Long> styleIds, Map<Long, Style> styleMap) {
        List<HistoryStyle> historyStyles =
                styleIds.stream()
                        .map(styleMap::get)
                        .map(style -> HistoryStyle.createHistoryStyle(history, style))
                        .toList();

        historyStyleRepository.bulkInsertHistoryStyles(historyStyles);
    }

    private void saveHistoryHashtags(History history, List<String> hashtags) {
        List<String> normalized = normalizeHashtags(hashtags);
        if (normalized.isEmpty()) return;

        Map<String, Hashtag> existing =
                hashtagRepository.findAllByNameIn(normalized).stream()
                        .collect(Collectors.toMap(Hashtag::getName, Function.identity()));

        List<Hashtag> toCreate =
                normalized.stream()
                        .distinct()
                        .filter(name -> !existing.containsKey(name))
                        .map(Hashtag::createHashtag)
                        .toList();

        if (!toCreate.isEmpty()) {
            List<Hashtag> saved = hashtagRepository.saveAll(toCreate);
            for (Hashtag h : saved) existing.put(h.getName(), h);
        }

        List<HistoryHashtag> links =
                normalized.stream()
                        .distinct()
                        .map(existing::get)
                        .map(h -> HistoryHashtag.createHistoryHashtag(history, h))
                        .toList();

        if (!links.isEmpty()) {
            historyHashtagRepository.bulkInsertHistoryHashtags(links);
        }
    }

    private void validateDuplicatedStyleIds(List<Long> styleIds) {
        if (styleIds.size() != new HashSet<>(styleIds).size()) {
            throw new BaseCustomException(StyleErrorCode.DUPLICATED_STYLE);
        }
    }

    private Situation getSituationById(Long id) {
        return situationRepository
                .findById(id)
                .orElseThrow(() -> new BaseCustomException(SituationErrorCode.SITUATION_NOT_FOUND));
    }

    private void validateStyleIds(List<Long> requested, Map<Long, Style> styleMap) {
        if (styleMap.size() != requested.size()) {
            throw new BaseCustomException(StyleErrorCode.STYLE_NOT_FOUND);
        }
    }

    private Map<Long, Style> getStylesByIds(List<Long> ids) {
        return styleRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Style::getId, Function.identity()));
    }

    private Map<Long, Style> validateAndLoadStyles(List<Long> styleIds) {
        validateDuplicatedStyleIds(styleIds);
        List<Long> distinctStyleIds = styleIds.stream().distinct().toList();
        Map<Long, Style> styleMap = getStylesByIds(distinctStyleIds);
        validateStyleIds(styleIds, styleMap);
        return styleMap;
    }

    private Map<Long, Cloth> validateAndLoadClothes(Member member, List<Long> clothIds) {
        if (clothIds == null || clothIds.isEmpty()) {
            return Map.of();
        }

        validateDuplicatedClothIds(clothIds);

        Map<Long, Cloth> clothMap =
                clothRepository.findAllById(clothIds).stream()
                        .collect(Collectors.toMap(Cloth::getId, Function.identity()));

        validateAllClothesExist(clothIds, clothMap);
        validateAllClothesOwnership(member, new ArrayList<>(clothMap.values()));

        return clothMap;
    }

    private void validateDuplicatedClothIds(List<Long> clothIds) {
        Set<Long> seen = new HashSet<>();
        for (Long id : clothIds) {
            if (!seen.add(id)) {
                throw new BaseCustomException(ClothErrorCode.DUPLICATED_CLOTH);
            }
        }
    }

    private void validateAllClothesExist(List<Long> clothIds, Map<Long, Cloth> clothMap) {
        boolean hasMissing = clothIds.stream().anyMatch(clothId -> !clothMap.containsKey(clothId));

        if (hasMissing) {
            throw new BaseCustomException(ClothErrorCode.ClOTH_NOT_FOUND);
        }
    }

    private void validateAllClothesOwnership(Member member, List<Cloth> clothes) {
        boolean containsClothesNotMine =
                clothes.stream()
                        .anyMatch(cloth -> !cloth.getMember().getId().equals(member.getId()));

        if (containsClothesNotMine) {
            throw new BaseCustomException(ClothErrorCode.NOT_CLOTH_OWNER);
        }
    }

    private List<String> normalizeHashtags(List<String> raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(s -> s.startsWith("#") ? s.substring(1) : s)
                .map(s -> s.toLowerCase(Locale.ROOT).replaceAll("\\s+", ""))
                .filter(s -> !s.isBlank())
                .toList();
    }

    private History getHistoryById(Long historyId) {
        return historyRepository
                .findById(historyId)
                .orElseThrow(() -> new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND));
    }

    private void validateHistoryOwner(History history, Long memberId) {
        if (!Objects.equals(history.getMember().getId(), memberId)) {
            throw new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY);
        }
    }

    private void clearStylesAndHashtags(Long historyId) {
        List<HistoryStyle> styles = historyStyleRepository.findByHistoryId(historyId);
        if (!styles.isEmpty()) {
            historyStyleRepository.deleteAll(styles);
        }

        List<HistoryHashtag> hashtags = historyHashtagRepository.findByHistoryId(historyId);
        if (!hashtags.isEmpty()) {
            historyHashtagRepository.deleteAll(hashtags);
        }
    }

    private void deleteClothTagsByHistoryImageIds(List<Long> historyImageIds) {
        if (historyImageIds == null || historyImageIds.isEmpty()) {
            return;
        }
        historyClothTagRepository.deleteAllByHistoryImageIdIn(historyImageIds);
    }

    private Member getMemberById(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(
                        () ->
                                new BaseCustomException(
                                        org.clokey.domain.member.exception.MemberErrorCode
                                                .MEMBER_NOT_FOUND));
    }

    private void validateMemberAccess(Long currentMemberId, Member targetMember) {
        // 유저가 비공개이고 본인이 아닌 경우 접근 불가
        if (targetMember.getVisibility() == Visibility.PRIVATE
                && !targetMember.getId().equals(currentMemberId)) {
            throw new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY);
        }

        // 유저를 차단했거나 차단 당한 경우
        if (blockRepository.existsByBlockerIdAndBlockedId(targetMember.getId(), currentMemberId)
                || blockRepository.existsByBlockerIdAndBlockedId(
                        currentMemberId, targetMember.getId())) {
            throw new BaseCustomException(HistoryErrorCode.BLOCKED_AUTHORITY);
        }
    }

    private void validateBlockedAccess(Long currentMemberId, History history) {
        // 기록 작성자가 비공개이고 본인 기록이 아닌 경우 접근 불가
        if (history.getMember().getVisibility() == Visibility.PRIVATE
                && !history.getMember().getId().equals(currentMemberId)) {
            throw new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY);
        }

        // 기록 작성자를 차단했거나 차단 당한 경우
        if (blockRepository.existsByBlockerIdAndBlockedId(
                        history.getMember().getId(), currentMemberId)
                || blockRepository.existsByBlockerIdAndBlockedId(
                        currentMemberId, history.getMember().getId())) {
            throw new BaseCustomException(HistoryErrorCode.BLOCKED_AUTHORITY);
        }

        // 기록이 신고로 인해 ban 당한 경우
        if (history.isBanned()) {
            throw new BaseCustomException(HistoryErrorCode.BANNED_HISTORY);
        }
    }
}
