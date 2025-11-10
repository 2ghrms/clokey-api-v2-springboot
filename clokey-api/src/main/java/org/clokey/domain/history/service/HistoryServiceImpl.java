package org.clokey.domain.history.service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.domain.cloth.exception.ClothErrorCode;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.domain.history.repository.*;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.*;
import org.clokey.member.entity.Member;
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

    @Override
    @Transactional
    public HistoryCreateResponse createHistory(HistoryCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        final Situation situation = getSituationById(request.situationId());

        final List<Long> styleIds = request.styleIds();
        validateDuplicatedStyleIds(styleIds);
        final List<Long> distinctStyleIds = styleIds.stream().distinct().toList();
        final Map<Long, Style> styleMap = getStylesByIds(distinctStyleIds);
        validateStyleIds(styleIds, styleMap);

        final List<Long> allRequestedClothIds =
                request.payloads().stream()
                        .filter(p -> p.clothTags() != null)
                        .flatMap(p -> p.clothTags().stream())
                        .map(HistoryCreateRequest.ClothTag::clothId)
                        .toList();

        validateDuplicatedClothIds(allRequestedClothIds);

        final Map<Long, Cloth> clothMap =
                clothRepository.findAllById(allRequestedClothIds).stream()
                        .collect(Collectors.toMap(Cloth::getId, Function.identity()));

        validateAllClothesExist(allRequestedClothIds.stream().toList(), clothMap);
        validateAllClothesOwnership(currentMember, clothMap.values().stream().toList());

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
        historyImageRepository.saveAll(images);

        if (!clothTags.isEmpty()) {
            historyClothTagRepository.bulkInsertHistoryClothTags(clothTags);
        }

        final List<HistoryStyle> historyStyles =
                styleIds.stream()
                        .map(styleMap::get)
                        .map(style -> HistoryStyle.createHistoryStyle(history, style))
                        .toList();
        historyStyleRepository.bulkInsertHistoryStyles(historyStyles);

        final List<String> normalized = normalizeHashtags(request.hashtags());

        if (!normalized.isEmpty()) {
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

            if (!links.isEmpty()) historyHashtagRepository.bulkInsertHistoryHashtags(links);
        }

        return new HistoryCreateResponse(history.getId());
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
}
