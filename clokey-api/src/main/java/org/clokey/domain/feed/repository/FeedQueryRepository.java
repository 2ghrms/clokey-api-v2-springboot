package org.clokey.domain.feed.repository;

import static org.clokey.history.entity.QHistory.history;
import static org.clokey.history.entity.QHistoryStyle.historyStyle;
import static org.clokey.member.entity.QFollow.follow;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.feed.query.FeedCursor;
import org.clokey.domain.feed.query.FollowScope;
import org.clokey.history.entity.History;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedQueryRepository {

    private final JPAQueryFactory queryFactory;

    // Recommended indexes:
    // - history(created_at, id)
    // - member_like(member_id, history_id)
    // - follow(follow_from_id, follow_to_id)
    // - history_style(history_id, style_id), history(situation_id)
    public List<History> findFeeds(
            Long currentMemberId,
            FollowScope followScope,
            List<Long> styleIds,
            List<Long> situationIds,
            FeedCursor cursor,
            int size) {
        return queryFactory
                .selectFrom(history)
                .join(history.member, member)
                .fetchJoin()
                .where(
                        history.banned.isFalse(),
                        followScopeCondition(currentMemberId, followScope),
                        styleFilterCondition(styleIds),
                        situationFilterCondition(situationIds),
                        cursorCondition(cursor))
                .orderBy(history.createdAt.desc(), history.id.desc())
                .limit((long) size + 1)
                .fetch();
    }

    public List<History> findFeedsByIds(List<Long> historyIds) {
        if (historyIds == null || historyIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .selectFrom(history)
                .join(history.member, member)
                .fetchJoin()
                .where(history.banned.isFalse(), history.id.in(historyIds))
                .fetch();
    }

    private BooleanExpression followScopeCondition(Long currentMemberId, FollowScope followScope) {
        if (followScope == null || followScope == FollowScope.ALL) {
            return null;
        }

        return history.member.id.in(
                JPAExpressions.select(follow.followTo.id)
                        .from(follow)
                        .where(follow.followFrom.id.eq(currentMemberId)));
    }

    private BooleanExpression styleFilterCondition(List<Long> styleIds) {
        if (styleIds == null || styleIds.isEmpty()) {
            return null;
        }

        return JPAExpressions.selectOne()
                .from(historyStyle)
                .where(historyStyle.history.id.eq(history.id), historyStyle.style.id.in(styleIds))
                .exists();
    }

    private BooleanExpression situationFilterCondition(List<Long> situationIds) {
        if (situationIds == null || situationIds.isEmpty()) {
            return null;
        }

        return history.situation.id.in(situationIds);
    }

    private BooleanExpression cursorCondition(FeedCursor cursor) {
        if (cursor == null) {
            return null;
        }
        LocalDateTime createdAt = cursor.createdAt();
        Long feedId = cursor.feedId();
        if (createdAt == null || feedId == null) {
            return null;
        }

        return history.createdAt
                .lt(createdAt)
                .or(history.createdAt.eq(createdAt).and(history.id.lt(feedId)));
    }
}
