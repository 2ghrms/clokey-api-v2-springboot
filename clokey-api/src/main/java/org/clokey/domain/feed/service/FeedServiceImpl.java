package org.clokey.domain.feed.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.feed.dto.response.FeedListResponse;
import org.clokey.domain.feed.query.FeedCursor;
import org.clokey.domain.feed.query.FollowScope;
import org.clokey.domain.feed.repository.FeedQueryRepository;
import org.clokey.domain.feed.util.FeedCursorUtil;
import org.clokey.domain.feed.util.FeedRequestParser;
import org.clokey.domain.history.exception.SituationErrorCode;
import org.clokey.domain.history.exception.StyleErrorCode;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.SituationRepository;
import org.clokey.domain.history.repository.StyleRepository;
import org.clokey.domain.like.repository.MemberLikeRepository;
import org.clokey.domain.member.repository.FollowRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.Situation;
import org.clokey.history.entity.Style;
import org.clokey.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private static final int ALL_OVERFETCH_MULTIPLIER = 3;

    private final MemberUtil memberUtil;
    private final FeedQueryRepository feedQueryRepository;
    private final HistoryImageRepository historyImageRepository;
    private final MemberLikeRepository memberLikeRepository;
    private final FollowRepository followRepository;
    private final StyleRepository styleRepository;
    private final SituationRepository situationRepository;

    @Override
    public FeedListResponse getFeeds(
            FollowScope followScope,
            List<Long> styleIds,
            List<Long> situationIds,
            Integer size,
            String cursor) {
        final Member currentMember = memberUtil.getCurrentMember();
        final int pageSize = FeedRequestParser.parseSize(size, 10, 50);
        final FollowScope resolvedScope = followScope == null ? FollowScope.ALL : followScope;
        final FeedCursor decodedCursor = FeedCursorUtil.decode(cursor);
        final List<Long> resolvedStyleIds = styleIds == null ? List.of() : styleIds;
        final List<Long> resolvedSituationIds = situationIds == null ? List.of() : situationIds;
        validateFilters(resolvedStyleIds, resolvedSituationIds);
        final List<Long> pendingFeedIds =
                decodedCursor == null || decodedCursor.pendingFeedIds() == null
                        ? List.of()
                        : decodedCursor.pendingFeedIds();
        final List<Long> basePendingIds = new ArrayList<>(pendingFeedIds);

        List<History> selectedHistories = new ArrayList<>();
        List<Long> remainingPendingIds = List.of();
        if (!basePendingIds.isEmpty()) {
            int takeCount = Math.min(pageSize, basePendingIds.size());
            List<Long> takeIds = basePendingIds.subList(0, takeCount);
            remainingPendingIds = basePendingIds.subList(takeCount, basePendingIds.size());
            List<History> pendingHistories = feedQueryRepository.findFeedsByIds(takeIds);
            Map<Long, History> pendingMap =
                    pendingHistories.stream()
                            .collect(Collectors.toMap(History::getId, history -> history));
            for (Long id : takeIds) {
                History history = pendingMap.get(id);
                if (history != null) {
                    selectedHistories.add(history);
                }
            }
        }

        int remainingSlots = pageSize - selectedHistories.size();
        List<History> fetchedHistories = List.of();
        boolean fetchedHasNext = false;
        List<Long> nextPendingIds = new ArrayList<>(remainingPendingIds);
        if (remainingSlots > 0) {
            int fetchSize =
                    resolvedScope == FollowScope.ALL
                            ? remainingSlots * ALL_OVERFETCH_MULTIPLIER
                            : remainingSlots;
            fetchedHistories =
                    feedQueryRepository.findFeeds(
                            currentMember.getId(),
                            resolvedScope,
                            resolvedStyleIds,
                            resolvedSituationIds,
                            decodedCursor,
                            fetchSize);
            fetchedHasNext = fetchedHistories.size() > fetchSize;
            if (fetchedHasNext) {
                fetchedHistories = fetchedHistories.subList(0, fetchSize);
            }

            if (resolvedScope == FollowScope.ALL) {
                List<History> interleaved = interleaveByAuthor(fetchedHistories, remainingSlots);
                selectedHistories.addAll(interleaved);
                List<Long> skippedIds = extractSkippedIds(fetchedHistories, interleaved);
                if (!nextPendingIds.isEmpty()) {
                    nextPendingIds.addAll(skippedIds);
                } else {
                    nextPendingIds = skippedIds;
                }
            } else {
                selectedHistories.addAll(fetchedHistories);
            }
        }

        boolean hasNext = fetchedHasNext || !nextPendingIds.isEmpty();
        if (selectedHistories.isEmpty() && !hasNext) {
            return FeedListResponse.of(List.of(), null, false);
        }

        List<Long> feedIds = selectedHistories.stream().map(History::getId).toList();
        List<Long> authorIds =
                selectedHistories.stream().map(h -> h.getMember().getId()).distinct().toList();

        Map<Long, String> imageUrlMap = getImageUrls(feedIds);
        Set<Long> likedHistoryIds = getLikedHistoryIds(currentMember.getId(), feedIds);
        Set<Long> followedMemberIds = getFollowedMemberIds(currentMember.getId(), authorIds);
        List<FeedListResponse.FeedItemResponse> items =
                selectedHistories.stream()
                        .map(
                                history ->
                                        new FeedListResponse.FeedItemResponse(
                                                history.getId(),
                                                history.getCreatedAt(),
                                                imageUrlMap.get(history.getId()),
                                                likedHistoryIds.contains(history.getId()),
                                                toAuthorResponse(
                                                        history.getMember(),
                                                        followedMemberIds.contains(
                                                                history.getMember().getId()))))
                        .toList();

        String nextCursorValue = null;
        if (hasNext) {
            if (!fetchedHistories.isEmpty()) {
                History last = fetchedHistories.get(fetchedHistories.size() - 1);
                nextCursorValue =
                        FeedCursorUtil.encode(last.getCreatedAt(), last.getId(), nextPendingIds);
            } else if (decodedCursor != null) {
                nextCursorValue =
                        FeedCursorUtil.encode(
                                decodedCursor.createdAt(), decodedCursor.feedId(), nextPendingIds);
            }
        }

        return FeedListResponse.of(items, nextCursorValue, hasNext);
    }

    private FeedListResponse.FeedAuthorResponse toAuthorResponse(
            Member member, boolean isFollowing) {
        return new FeedListResponse.FeedAuthorResponse(
                member.getId(), member.getClokeyId(), member.getProfileImageUrl(), isFollowing);
    }

    private Map<Long, String> getImageUrls(List<Long> feedIds) {
        if (feedIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = historyImageRepository.getFirstImageUrlsWithHistoryId(feedIds);
        Map<Long, String> map = new HashMap<>();
        for (Object[] row : rows) {
            Long historyId = (Long) row[0];
            String imageUrl = (String) row[1];
            map.put(historyId, imageUrl);
        }
        return map;
    }

    private Set<Long> getLikedHistoryIds(Long memberId, List<Long> feedIds) {
        if (feedIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(memberLikeRepository.findLikedHistoryIds(memberId, feedIds));
    }

    private Set<Long> getFollowedMemberIds(Long memberId, List<Long> authorIds) {
        if (authorIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(followRepository.findFollowedMemberIds(memberId, authorIds));
    }

    private void validateFilters(List<Long> styleIds, List<Long> situationIds) {
        if (!styleIds.isEmpty()) {
            int count = 0;
            for (Style style : styleRepository.findAllById(styleIds)) {
                count++;
            }
            if (count != styleIds.size()) {
                throw new BaseCustomException(StyleErrorCode.STYLE_NOT_FOUND);
            }
        }

        if (!situationIds.isEmpty()) {
            int count = 0;
            for (Situation situation : situationRepository.findAllById(situationIds)) {
                count++;
            }
            if (count != situationIds.size()) {
                throw new BaseCustomException(SituationErrorCode.SITUATION_NOT_FOUND);
            }
        }
    }

    private List<History> interleaveByAuthor(List<History> histories, int limit) {
        if (histories.isEmpty() || limit <= 0) {
            return List.of();
        }
        Map<Long, ArrayDeque<History>> byAuthor = new LinkedHashMap<>();
        for (History history : histories) {
            Long memberId = history.getMember().getId();
            byAuthor.computeIfAbsent(memberId, id -> new ArrayDeque<>()).add(history);
        }

        List<History> interleaved = new ArrayList<>();
        int remaining = histories.size();
        while (interleaved.size() < limit && remaining > 0) {
            boolean added = false;
            for (ArrayDeque<History> queue : byAuthor.values()) {
                if (queue.isEmpty() || interleaved.size() >= limit) {
                    continue;
                }
                interleaved.add(queue.pollFirst());
                remaining--;
                added = true;
            }
            if (!added) {
                break;
            }
        }
        return interleaved;
    }

    private List<Long> extractSkippedIds(
            List<History> baseHistories, List<History> returnedHistories) {
        if (baseHistories.isEmpty()) {
            return List.of();
        }
        Set<Long> returnedIds =
                returnedHistories.stream().map(History::getId).collect(Collectors.toSet());
        List<Long> skippedIds = new ArrayList<>();
        for (History history : baseHistories) {
            if (!returnedIds.contains(history.getId())) {
                skippedIds.add(history.getId());
            }
        }
        return skippedIds;
    }
}
