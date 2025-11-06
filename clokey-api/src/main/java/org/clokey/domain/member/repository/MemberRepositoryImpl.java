package org.clokey.domain.member.repository;

import static org.clokey.member.entity.QFollow.follow;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.response.MemberInfoResponse;
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
                                        .where(follow.followTo.id.eq(member.id)),
                                JPAExpressions.select(follow.count())
                                        .from(follow)
                                        .where(follow.followFrom.id.eq(member.id)),
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
}
