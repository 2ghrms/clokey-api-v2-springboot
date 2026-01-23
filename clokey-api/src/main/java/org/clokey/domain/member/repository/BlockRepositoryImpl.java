package org.clokey.domain.member.repository;

import static org.clokey.member.entity.QBlock.block;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.member.dto.response.BlockedMemberResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BlockRepositoryImpl implements BlockRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<BlockedMemberResponse> findAllByBlockerId(
            Long blockerId, Long lastBlockId, Integer size, SortDirection direction) {
        List<BlockedMemberResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        BlockedMemberResponse.class,
                                        block.id,
                                        member.id,
                                        member.clokeyId,
                                        member.profileImageUrl))
                        .from(block)
                        .join(block.blocked, member)
                        .where(
                                block.blocker.id.eq(blockerId),
                                lastBlockIdCondition(lastBlockId, direction))
                        .limit(size + 1)
                        .orderBy(direction == SortDirection.DESC ? block.id.desc() : block.id.asc())
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression lastBlockIdCondition(Long blockId, SortDirection direction) {
        if (blockId == null) {
            return null;
        }

        return direction == SortDirection.DESC ? block.id.lt(blockId) : block.id.gt(blockId);
    }

    @Override
    public List<Long> findBlockedMemberIdsByBlockerId(Long blockerId) {
        return queryFactory
                .select(block.blocked.id)
                .from(block)
                .where(block.blocker.id.eq(blockerId))
                .distinct()
                .fetch();
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
