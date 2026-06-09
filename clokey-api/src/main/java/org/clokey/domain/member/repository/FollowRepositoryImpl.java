package org.clokey.domain.member.repository;

import static org.clokey.member.entity.QFollow.follow;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.response.FollowMemberResponse;
import org.clokey.member.entity.QBlock;
import org.clokey.member.entity.QFollow;
import org.clokey.member.enums.Visibility;
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
        BooleanExpression isFollowingExpression =
                JPAExpressions.selectOne()
                        .from(followSub)
                        .where(
                                followSub.followFrom.id.eq(currentId),
                                followSub.followTo.id.eq(member.id))
                        .exists();
        BooleanExpression isMeExpression = member.id.eq(currentId);

        List<Tuple> tuples =
                queryFactory
                        .select(
                                follow.id,
                                member.id,
                                member.nickname,
                                member.profileImageUrl,
                                member.visibility,
                                isFollowingExpression,
                                isMeExpression)
                        .from(follow)
                        .join(follow.followTo, member)
                        .where(
                                follow.followFrom.id.eq(targetId),
                                lastFollowIdCondition(lastFollowId),
                                blockFilter(currentId))
                        .limit(size + 1)
                        .orderBy(follow.id.desc())
                        .fetch();

        List<FollowMemberResponse> results =
                mapToFollowMemberResponses(tuples, isFollowingExpression, isMeExpression);

        return checkLastPage(size, results);
    }

    @Override
    public Slice<FollowMemberResponse> findAllFollowersByMemberId(
            Long currentId, Long targetId, Long lastFollowId, Integer size) {
        QFollow followSub = new QFollow("followSub");
        BooleanExpression isFollowingExpression =
                JPAExpressions.selectOne()
                        .from(followSub)
                        .where(
                                followSub.followFrom.id.eq(currentId),
                                followSub.followTo.id.eq(member.id))
                        .exists();
        BooleanExpression isMeExpression = member.id.eq(currentId);

        List<Tuple> tuples =
                queryFactory
                        .select(
                                follow.id,
                                member.id,
                                member.nickname,
                                member.profileImageUrl,
                                member.visibility,
                                isFollowingExpression,
                                isMeExpression)
                        .from(follow)
                        .join(follow.followFrom, member)
                        .where(
                                follow.followTo.id.eq(targetId),
                                lastFollowIdCondition(lastFollowId),
                                blockFilter(currentId))
                        .limit(size + 1)
                        .orderBy(follow.id.desc())
                        .fetch();

        List<FollowMemberResponse> results =
                mapToFollowMemberResponses(tuples, isFollowingExpression, isMeExpression);

        return checkLastPage(size, results);
    }

    private List<FollowMemberResponse> mapToFollowMemberResponses(
            List<Tuple> tuples,
            Expression<Boolean> isFollowingExpression,
            Expression<Boolean> isMeExpression) {
        List<FollowMemberResponse> responses = new ArrayList<>(tuples.size());

        for (Tuple tuple : tuples) {
            responses.add(
                    new FollowMemberResponse(
                            tuple.get(follow.id),
                            tuple.get(member.id),
                            tuple.get(member.nickname),
                            tuple.get(member.profileImageUrl),
                            Visibility.PUBLIC.equals(tuple.get(member.visibility)),
                            Boolean.TRUE.equals(tuple.get(isFollowingExpression)),
                            Boolean.TRUE.equals(tuple.get(isMeExpression))));
        }

        return responses;
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
