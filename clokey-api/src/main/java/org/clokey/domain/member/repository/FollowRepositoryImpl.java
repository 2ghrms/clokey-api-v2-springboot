package org.clokey.domain.member.repository;

import static org.clokey.member.entity.QFollow.follow;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.response.FollowMemberResponse;
import org.clokey.member.entity.QBlock;
import org.clokey.member.entity.QFollow;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<FollowMemberResponse> findAllFollowingsByMemberId(
            Long currentId, Long targetId, Long lastFollowId, Integer size) {
        QFollow followSub = new QFollow("followSub");

        List<FollowMemberResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        FollowMemberResponse.class,
                                        follow.id,
                                        member.id,
                                        member.nickname,
                                        member.profileImageUrl,
                                        JPAExpressions.selectOne()
                                                .from(followSub)
                                                .where(
                                                        followSub.followFrom.id.eq(currentId),
                                                        followSub.followTo.id.eq(member.id))
                                                .exists(),
                                        member.id.eq(currentId)))
                        .from(follow)
                        .join(follow.followTo, member)
                        .where(
                                follow.followFrom.id.eq(targetId),
                                lastFollowIdCondition(lastFollowId),
                                blockFilter(currentId))
                        .limit(size + 1)
                        .orderBy(follow.id.desc())
                        .fetch();

        return checkLastPage(size, results);
    }

    @Override
    public Slice<FollowMemberResponse> findAllFollowersByMemberId(
            Long currentId, Long targetId, Long lastFollowId, Integer size) {
        QFollow followSub = new QFollow("followSub");

        List<FollowMemberResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        FollowMemberResponse.class,
                                        follow.id,
                                        member.id,
                                        member.nickname,
                                        member.profileImageUrl,
                                        JPAExpressions.selectOne()
                                                .from(followSub)
                                                .where(
                                                        followSub.followFrom.id.eq(currentId),
                                                        followSub.followTo.id.eq(member.id))
                                                .exists(),
                                        member.id.eq(currentId)))
                        .from(follow)
                        .join(follow.followFrom, member)
                        .where(
                                follow.followTo.id.eq(targetId),
                                lastFollowIdCondition(lastFollowId),
                                blockFilter(currentId))
                        .limit(size + 1)
                        .orderBy(follow.id.desc())
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression blockFilter(Long currentId) {
        QBlock blockCheck = new QBlock("blockCheck");

        BooleanExpression blockCondition =
                blockCheck
                        .blocker
                        .id
                        .eq(currentId)
                        .and(blockCheck.blocked.id.eq(member.id))
                        .or(
                                blockCheck
                                        .blocker
                                        .id
                                        .eq(member.id)
                                        .and(blockCheck.blocked.id.eq(currentId)));

        return Expressions.asBoolean(
                        JPAExpressions.selectOne().from(blockCheck).where(blockCondition).exists())
                .not();
    }

    private BooleanExpression lastFollowIdCondition(Long lastFollowId) {
        if (lastFollowId == null) {
            return null;
        }

        return follow.id.lt(lastFollowId);
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
