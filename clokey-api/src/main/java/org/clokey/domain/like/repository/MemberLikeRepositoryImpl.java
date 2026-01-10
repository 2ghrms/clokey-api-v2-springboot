package org.clokey.domain.like.repository;

import static org.clokey.like.entity.QMemberLike.memberLike;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.like.dto.response.LikedHistoriesResponse;
import org.clokey.domain.like.dto.response.LikedMembersResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberLikeRepositoryImpl implements MemberLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final SortDirection DEFAULT_SORT = SortDirection.DESC;

    @Override
    public Slice<LikedHistoriesResponse.LikedHistoryPreview> findLikedHistoriesSliceByMemberId(
            Long memberId, Long lastLikeId, Integer size) {

        List<LikedHistoriesResponse.LikedHistoryPreview> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        LikedHistoriesResponse.LikedHistoryPreview.class,
                                        memberLike.history.id,
                                        memberLike.id))
                        .from(memberLike)
                        .where(
                                memberLike.member.id.eq(memberId),
                                lastLikeIdCondition(lastLikeId, DEFAULT_SORT))
                        .limit(size + 1)
                        .orderBy(memberLike.id.desc())
                        .fetch();

        return checkLastPage(size, results);
    }

    @Override
    public Slice<LikedMembersResponse.LikedMemberPreview> findLikedMembersSliceByHistoryId(
            Long historyId, Long lastLikeId, Integer size) {

        List<LikedMembersResponse.LikedMemberPreview> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        LikedMembersResponse.LikedMemberPreview.class,
                                        member.id,
                                        member.clokeyId,
                                        member.profileImageUrl,
                                        member.nickname,
                                        memberLike.id))
                        .from(memberLike)
                        .join(memberLike.member, member)
                        .where(
                                memberLike.history.id.eq(historyId),
                                lastLikeIdCondition(lastLikeId, DEFAULT_SORT))
                        .limit(size + 1)
                        .orderBy(memberLike.id.desc())
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression lastLikeIdCondition(Long likeId, SortDirection direction) {
        if (likeId == null) {
            return null;
        }
        return memberLike.id.lt(likeId);
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
