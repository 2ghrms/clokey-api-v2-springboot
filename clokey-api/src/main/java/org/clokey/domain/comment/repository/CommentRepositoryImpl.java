package org.clokey.domain.comment.repository;

import static org.clokey.comment.entitiy.QComment.comment;
import static org.clokey.comment.entitiy.QReply.reply;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<CommentListResponse> findAllByHistoryId(
            Long historyId, Long lastCommentId, int size, SortDirection direction) {

        // 삼중 조인을 피하기 위해 댓글 존재 여부는 처음에는 false로 가져옵니다.
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
                                        Expressions.constant(false)))
                        .from(comment)
                        .join(comment.member, member)
                        .where(
                                comment.history.id.eq(historyId),
                                lastCommentIdCondition(lastCommentId, direction))
                        .orderBy(
                                direction == SortDirection.DESC
                                        ? comment.id.desc()
                                        : comment.id.asc())
                        .limit(size + 1)
                        .fetch();

        boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }

        Map<Long, Boolean> repliedMap =
                results.isEmpty()
                        ? Map.of()
                        : queryFactory
                                .select(reply.comment.id)
                                .from(reply)
                                .where(
                                        reply.comment.id.in(
                                                results.stream()
                                                        .map(CommentListResponse::commentId)
                                                        .toList()))
                                .groupBy(reply.comment.id)
                                .transform(
                                        GroupBy.groupBy(reply.comment.id)
                                                .as(Expressions.constant(true)));

        List<CommentListResponse> finalResults =
                results.stream()
                        .map(
                                c ->
                                        new CommentListResponse(
                                                c.commentId(),
                                                c.memberId(),
                                                c.nickName(),
                                                c.profileImageUrl(),
                                                c.content(),
                                                repliedMap.getOrDefault(c.commentId(), false)))
                        .toList();

        return new SliceImpl<>(finalResults, PageRequest.of(0, size), hasNext);
    }

    private BooleanExpression lastCommentIdCondition(Long commentId, SortDirection direction) {
        if (commentId == null) {
            return null;
        }

        return direction == SortDirection.DESC
                ? comment.id.lt(commentId)
                : comment.id.gt(commentId);
    }
}
