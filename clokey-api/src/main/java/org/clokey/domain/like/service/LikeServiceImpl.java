package org.clokey.domain.like.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.like.entity.MemberLike;
import org.clokey.member.entity.Member;
import org.clokey.response.SliceResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeServiceImpl implements LikeService {

    private final MemberUtil memberUtil;
    private final MemberLikeRepository memberLikeRepository;
    private final HistoryImageRepository historyImageRepository;
    private final HistoryRepository historyRepository;
    private final BlockRepository blockRepository;

    @Override
    public SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> getLikedHistories(
            Long lastLikeId, Integer size) {

        Member currentMember = memberUtil.getCurrentMember();

        // limit + 1 조회
        Pageable pageable = PageRequest.of(0, size + 1);

        List<MemberLike> likes =
                memberLikeRepository.findLikedHistoriesByMemberId(
                        currentMember.getId(), lastLikeId, pageable);

        boolean isLast = likes.size() <= size;

        if (!isLast) {
            likes = likes.subList(0, size);
        }

        if (likes.isEmpty()) {
            return new SliceResponse<>(List.of(), true);
        }

        List<Long> historyIds = likes.stream().map(like -> like.getHistory().getId()).toList();

        Map<Long, String> imageMap = findFirstImagesByHistoryIds(historyIds);

        List<LikedHistoriesResponse.LikedHistoryPreview> previews =
                likes.stream()
                        .map(
                                like ->
                                        new LikedHistoriesResponse.LikedHistoryPreview(
                                                like.getHistory().getId(),
                                                imageMap.get(like.getHistory().getId())))
                        .toList();

        return new SliceResponse<>(previews, isLast);
    }

    private Map<Long, String> findFirstImagesByHistoryIds(List<Long> historyIds) {
        if (historyIds.isEmpty()) return Map.of();

        List<Object[]> rows = historyImageRepository.getFirstImageUrlsWithHistoryId(historyIds);

        return rows.stream()
                .collect(
                        Collectors.toMap(
                                row -> ((Number) row[0]).longValue(), row -> (String) row[1]));
    }

    @Override
    @Transactional
    public void toggleLike(Long historyId) {
        final Member currentMember = memberUtil.getCurrentMember();
        final History history =
                historyRepository
                        .findByIdWithMember(historyId)
                        .orElseThrow(
                                () -> new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND));

        final Member historyOwner = history.getMember();

        if (isBlockedByOrBlocking(currentMember.getId(), historyOwner.getId())) {
            return;
        }

        Optional<MemberLike> existingLike =
                memberLikeRepository.findByMemberIdAndHistoryId(currentMember.getId(), historyId);

        if (existingLike.isPresent()) {
            memberLikeRepository.delete(existingLike.get());
        } else {
            MemberLike newLike = MemberLike.createMemberLike(currentMember, history);
            memberLikeRepository.save(newLike);
        }
    }

    private boolean isBlockedByOrBlocking(Long fromId, Long toId) {
        return blockRepository.existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
                fromId, toId,
                toId, fromId);
    }
}
