package org.clokey.domain.member.repository;

import static org.clokey.member.entity.QFollow.follow;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.response.MemberInfoResponse;
import org.clokey.member.entity.QBlock;
import org.clokey.member.entity.QFollow;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public MemberInfoResponse findMemberInfoById(Long currentId, Long targetId) {
        QFollow followSub = new QFollow("followSub");
        return queryFactory
                .select(
                        Projections.constructor(
                                MemberInfoResponse.class,
                                member.clokeyId,
                                member.nickname,
                                member.bio,
                                JPAExpressions.select(follow.count())
                                        .from(follow)
                                        .where(
                                                follow.followTo.id.eq(member.id),
                                                isNotBlocked(currentId, follow.followFrom.id)),
                                JPAExpressions.select(follow.count())
                                        .from(follow)
                                        .where(
                                                follow.followFrom.id.eq(member.id),
                                                isNotBlocked(currentId, follow.followTo.id)),
                                member.profileImageUrl,
                                JPAExpressions.selectOne()
                                        .from(followSub)
                                        .where(
                                                followSub.followFrom.id.eq(currentId),
                                                followSub.followTo.id.eq(member.id))
                                        .exists(),
                                member.id.eq(currentId)))
                .from(member)
                .where(member.id.eq(targetId))
                .fetchOne();
    }

    private BooleanExpression isNotBlocked(Long currentId, NumberPath<Long> targetMemberId) {
        if (currentId == null) {
            return null;
        }

        QBlock blockCheck = new QBlock("blockCheck");

        BooleanExpression blockCondition =
                blockCheck
                        .blocker
                        .id
                        .eq(currentId)
                        .and(blockCheck.blocked.id.eq(targetMemberId))
                        .or(
                                blockCheck
                                        .blocker
                                        .id
                                        .eq(targetMemberId)
                                        .and(blockCheck.blocked.id.eq(currentId)));

        return Expressions.asBoolean(
                        JPAExpressions.selectOne().from(blockCheck).where(blockCondition).exists())
                .not();
    }
}
