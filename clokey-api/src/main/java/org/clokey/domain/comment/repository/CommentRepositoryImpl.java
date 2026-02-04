package org.clokey.domain.comment.repository;

import static org.clokey.comment.entitiy.QComment.comment1;
import static org.clokey.history.entity.QHistory.history;
import static org.clokey.history.entity.QHistoryImage.historyImage;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.clokey.comment.entitiy.QComment;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.MyCommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QComment comment = comment1;

    @Override
    public Slice<CommentListResponse> findAllParentCommentByHistoryId(
            Long historyId,
            Long currentMemberId,
            Long lastCommentId,
            int size,
            SortDirection direction) {

        // 부모 댓글만 조회
        List<CommentListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        CommentListResponse.class,
                                        comment.id,
                                        member.id,
                                        member.nickname,
                                        member.profileImageUrl,
                                        comment.content,
                                        Expressions.constant(false),
                                        Expressions.constant(0L),
                                        member.id.eq(currentMemberId),
                                        member.id
                                                .eq(currentMemberId)
                                                .or(history.member.id.eq(currentMemberId))))
                        .from(comment)
                        .join(comment.member, member)
                        .where(
                                comment.history.id.eq(historyId),
                                comment.comment.isNull(),
                                lastCommentIdCondition(lastCommentId, direction))
                        .orderBy(
                                direction == SortDirection.DESC
                                        ? comment.id.desc()
                                        : comment.id.asc())
                        .limit((long) size + 1)
                        .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }

        NumberExpression<Long> replyCountExpression = comment.id.count();
        List<Tuple> replyCounts =
                queryFactory
                        .select(comment.comment.id, replyCountExpression)
                        .from(comment)
                        .where(
                                comment.comment.id.in(
                                        results.stream()
                                                .map(CommentListResponse::commentId)
                                                .toList()))
                        .groupBy(comment.comment.id)
                        .fetch();

        // 각 부모 댓글별 대댓글 개수 조회
        Map<Long, Long> replyCountMap =
                replyCounts.stream()
                        .collect(
                                Collectors.toMap(
                                        tuple -> tuple.get(comment.comment.id),
                                        tuple -> tuple.get(replyCountExpression)));

        List<CommentListResponse> finalResults =
                results.stream()
                        .map(
                                c ->
                                        new CommentListResponse(
                                                c.commentId(),
                                                c.memberId(),
                                                c.nickname(),
                                                c.profileImageUrl(),
                                                c.content(),
                                                replyCountMap.getOrDefault(c.commentId(), 0L) > 0,
                                                replyCountMap.getOrDefault(c.commentId(), 0L),
                                                c.isMine(),
                                                c.canDelete()))
                        .toList();

        return new SliceImpl<>(finalResults, PageRequest.of(0, size), hasNext);
    }

    @Override
    public Slice<ReplyListResponse> findAllRepliesByCommentId(
            Long commentId,
            Long currentMemberId,
            Long lastReplyId,
            int size,
            SortDirection direction) {

        List<ReplyListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        ReplyListResponse.class,
                                        comment.id,
                                        member.id,
                                        member.nickname,
                                        member.profileImageUrl,
                                        comment.content,
                                        member.id.eq(currentMemberId),
                                        member.id
                                                .eq(currentMemberId)
                                                .or(history.member.id.eq(currentMemberId))))
                        .from(comment)
                        .join(comment.member, member)
                        .where(
                                comment.comment.id.eq(commentId),
                                lastCommentIdCondition(lastReplyId, direction))
                        .orderBy(
                                direction == SortDirection.DESC
                                        ? comment.id.desc()
                                        : comment.id.asc())
                        .limit((long) size + 1)
                        .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }

        return new SliceImpl<>(results, PageRequest.of(0, size), hasNext);
    }

    @Override
    public Slice<MyCommentListResponse> findAllMyComments(
            Long myMemberId, Long lastHistoryId, int size, SortDirection direction) {
        // 내가 댓글을 작성한 historyId를 페이징으로 조회
        List<Long> historyIds =
                queryFactory
                        .select(comment.history.id)
                        .from(comment)
                        .where(
                                comment.member.id.eq(myMemberId),
                                lastHistoryIdCondition(lastHistoryId, direction))
                        .distinct()
                        .orderBy(
                                direction == SortDirection.DESC
                                        ? comment.history.id.desc()
                                        : comment.history.id.asc())
                        .limit((long) size + 1)
                        .fetch();

        boolean hasNext = historyIds.size() > size;
        if (hasNext) {
            historyIds = historyIds.subList(0, size);
        }

        if (historyIds.isEmpty()) {
            return new SliceImpl<>(List.of(), PageRequest.of(0, size), hasNext);
        }

        // 1. history + 작성자 정보 조회
        List<HistoryInfo> historyInfos =
                queryFactory
                        .select(
                                Projections.constructor(
                                        HistoryInfo.class,
                                        history.id,
                                        history.historyDate,
                                        history.content,
                                        member.nickname))
                        .from(history)
                        .join(history.member, member)
                        .where(history.id.in(historyIds))
                        .fetch();

        Map<Long, HistoryInfo> historyInfoMap =
                historyInfos.stream().collect(Collectors.toMap(HistoryInfo::historyId, h -> h));

        // 2. history별 대표 사진 조회 (첫 번째 사진)
        List<HistoryImageInfo> imageInfos =
                queryFactory
                        .select(
                                Projections.constructor(
                                        HistoryImageInfo.class,
                                        historyImage.history.id,
                                        historyImage.imageUrl))
                        .from(historyImage)
                        .where(historyImage.history.id.in(historyIds))
                        .orderBy(historyImage.id.asc())
                        .fetch();

        Map<Long, String> imageUrlMap =
                imageInfos.stream()
                        .collect(
                                Collectors.toMap(
                                        HistoryImageInfo::historyId,
                                        HistoryImageInfo::imageUrl,
                                        (first, second) -> first));

        // 3. 각 history 마다 댓글 조회
        List<CommentInfo> payloadRows =
                queryFactory
                        .select(
                                Projections.constructor(
                                        CommentInfo.class,
                                        comment.history.id,
                                        comment.id,
                                        comment.content))
                        .from(comment)
                        .where(comment.member.id.eq(myMemberId), comment.history.id.in(historyIds))
                        .orderBy(comment.id.asc())
                        .fetch();

        Map<Long, List<MyCommentListResponse.Payload>> payloadMap =
                payloadRows.stream()
                        .collect(
                                Collectors.groupingBy(
                                        CommentInfo::historyId,
                                        Collectors.mapping(
                                                r ->
                                                        new MyCommentListResponse.Payload(
                                                                r.commentId(), r.content()),
                                                Collectors.toList())));

        List<MyCommentListResponse> results =
                historyIds.stream()
                        .map(
                                hid -> {
                                    HistoryInfo info = historyInfoMap.get(hid);
                                    if (info == null) return null;

                                    List<MyCommentListResponse.Payload> payloads =
                                            new ArrayList<>(
                                                    payloadMap.getOrDefault(hid, List.of()));

                                    return new MyCommentListResponse(
                                            hid,
                                            imageUrlMap.get(hid),
                                            info.nickname(),
                                            info.historyDate(),
                                            info.content(),
                                            payloads);
                                })
                        .filter(java.util.Objects::nonNull)
                        .toList();

        return new SliceImpl<>(results, PageRequest.of(0, size), hasNext);
    }

    private BooleanExpression lastCommentIdCondition(Long commentId, SortDirection direction) {
        if (commentId == null) {
            return null;
        }

        return direction == SortDirection.DESC
                ? comment.id.lt(commentId)
                : comment.id.gt(commentId);
    }

    private BooleanExpression lastHistoryIdCondition(Long lastHistoryId, SortDirection direction) {
        if (lastHistoryId == null) {
            return null;
        }
        return direction == SortDirection.DESC
                ? comment.history.id.lt(lastHistoryId)
                : comment.history.id.gt(lastHistoryId);
    }

    public record HistoryInfo(
            Long historyId, java.time.LocalDate historyDate, String content, String nickname) {}

    public record HistoryImageInfo(Long historyId, String imageUrl) {}

    public record CommentInfo(Long historyId, Long commentId, String content) {}
}
