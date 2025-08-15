package org.clokey.domain.comment.repository;

import static org.clokey.comment.entitiy.QReply.reply;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReplyRepositoryImpl implements ReplyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ReplyListResponse> findAllByCommentId(
            Long commentId, Long lastReplyId, int size, SortDirection direction) {
        List<ReplyListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        ReplyListResponse.class,
                                        reply.id,
                                        member.id,
                                        member.nickname,
                                        member.profileImageUrl,
                                        reply.content))
                        .from(reply)
                        .join(reply.member, member)
                        .where(
                                reply.comment.id.eq(commentId),
                                lastReplyIdCondition(lastReplyId, direction))
                        .orderBy(direction == SortDirection.DESC ? reply.id.desc() : reply.id.asc())
                        .limit(size + 1)
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression lastReplyIdCondition(Long replyId, SortDirection direction) {
        if (replyId == null) {
            return null;
        }

        return direction == SortDirection.DESC ? reply.id.lt(replyId) : reply.id.gt(replyId);
    }

    private <T> Slice<T> checkLastPage(int pageSize, List<T> results) {
        boolean hasNext = false;

        if (results.size() > pageSize) {
            hasNext = true;
            results.remove(pageSize);
        }

        return new SliceImpl<>(results, PageRequest.of(0, pageSize), hasNext);
    }
}
