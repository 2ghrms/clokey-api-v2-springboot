package org.clokey.domain.lookbook.repository;

import static org.clokey.coordinate.entity.QCoordinate.coordinate;
import static org.clokey.lookbook.entity.QLookBook.lookBook;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.coordinate.entity.QCoordinate;
import org.clokey.domain.lookbook.dto.response.LookBookListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LookBookRepositoryImpl implements LookBookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<LookBookListResponse> findAllLookBookByMemberId(
            Long currentMemberId, Long lastLookBookId, int size, SortDirection direction) {
        QCoordinate firstCoordinate = new QCoordinate("firstCoordinate");

        List<LookBookListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        LookBookListResponse.class,
                                        lookBook.id,
                                        lookBook.name,
                                        JPAExpressions.select(coordinate.count())
                                                .from(coordinate)
                                                .where(coordinate.lookBook.id.eq(lookBook.id)),
                                        firstCoordinate.imageUrl))
                        .from(lookBook)
                        .leftJoin(firstCoordinate)
                        .on(
                                firstCoordinate.lookBook.id.eq(lookBook.id),
                                firstCoordinate.id.eq(
                                        JPAExpressions.select(coordinate.id.min())
                                                .from(coordinate)
                                                .where(coordinate.lookBook.id.eq(lookBook.id))))
                        .where(
                                lookBook.member.id.eq(currentMemberId),
                                lastLookBookIdCondition(lastLookBookId, direction))
                        .orderBy(
                                direction == SortDirection.DESC
                                        ? lookBook.id.desc()
                                        : lookBook.id.asc())
                        .limit(size + 1)
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression lastLookBookIdCondition(Long lookBookId, SortDirection direction) {
        if (lookBookId == null) {
            return null;
        }

        return direction == SortDirection.DESC
                ? lookBook.id.lt(lookBookId)
                : lookBook.id.gt(lookBookId);
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
