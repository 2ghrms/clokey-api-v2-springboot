package org.clokey.domain.like.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.dto.response.LikedMembersResponse;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.like.repository.MemberLikeRepositoryCustom;
import org.clokey.domain.member.repository.BlockRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.domain.search.event.MeiliSearchSyncEvent;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.like.entity.MemberLike;
import org.clokey.member.entity.Member;
import org.clokey.response.SliceResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
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
    private final FollowRepository followRepository;
    private final MemberLikeRepositoryCustom memberLikeRepositoryCustom;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public SliceResponse<LikedHistoriesResponse.LikedHistoryPreview> getLikedHistories(
            Long lastLikeId, Integer size) {

        Member currentMember = memberUtil.getCurrentMember();

        Slice<LikedHistoriesResponse.LikedHistoryPreview> likedHistoriesSlice =
                memberLikeRepositoryCustom.findLikedHistoriesSliceByMemberId(
                        currentMember.getId(), lastLikeId, size);

        if (likedHistoriesSlice.isEmpty()) {
            return new SliceResponse<>(List.of(), true);
        }

        List<Long> historyIds =
                likedHistoriesSlice.getContent().stream()
                        .map(LikedHistoriesResponse.LikedHistoryPreview::getId)
                        .toList();

        Map<Long, String> imageMap = findFirstImagesByHistoryIds(historyIds);

        List<LikedHistoriesResponse.LikedHistoryPreview> previews =
                likedHistoriesSlice.getContent().stream()
                        .map(
                                preview ->
                                        new LikedHistoriesResponse.LikedHistoryPreview(
                                                preview.getId(),
                                                imageMap.get(preview.getId()),
                                                preview.getHistoryDate()))
                        .toList();

        return new SliceResponse<>(previews, likedHistoriesSlice.isLast());
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

        eventPublisher.publishEvent(
                MeiliSearchSyncEvent.of(MeiliSearchSyncEvent.EntityType.HISTORY, historyId));
    }

    private boolean isBlockedByOrBlocking(Long fromId, Long toId) {
        return blockRepository.existsByBlockerIdAndBlockedIdOrBlockerIdAndBlockedId(
                fromId, toId,
                toId, fromId);
    }

    @Override
    public SliceResponse<LikedMembersResponse.LikedMemberPreview> getLikedMembers(
            Long historyId, Long lastLikeId, Integer size) {

        Member currentMember = memberUtil.getCurrentMember();

        Slice<LikedMembersResponse.LikedMemberPreview> likedMembersSlice =
                memberLikeRepositoryCustom.findLikedMembersSliceByHistoryId(
                        historyId, lastLikeId, size);

        if (likedMembersSlice.isEmpty()) {
            return new SliceResponse<>(List.of(), true);
        }

        List<Long> memberIds =
                likedMembersSlice.getContent().stream()
                        .map(LikedMembersResponse.LikedMemberPreview::getId)
                        .toList();

        Set<Long> followedIdSet =
                new HashSet<>(
                        followRepository.findFollowedMemberIds(currentMember.getId(), memberIds));

        List<LikedMembersResponse.LikedMemberPreview> previews =
                likedMembersSlice.getContent().stream()
                        .map(
                                preview ->
                                        new LikedMembersResponse.LikedMemberPreview(
                                                preview.getId(),
                                                preview.getCodiveId(),
                                                preview.getImageUrl(),
                                                preview.getNickname(),
                                                followedIdSet.contains(preview.getId())))
                        .toList();

        return new SliceResponse<>(previews, likedMembersSlice.isLast());
    }
}
