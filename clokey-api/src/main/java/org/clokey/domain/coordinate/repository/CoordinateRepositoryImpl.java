package org.clokey.domain.coordinate.repository;

import static org.clokey.category.entity.QCategory.category;
import static org.clokey.cloth.entity.QCloth.cloth;
import static org.clokey.coordinate.entity.QCoordinate.coordinate;
import static org.clokey.coordinate.entity.QCoordinateCloth.coordinateCloth;
import static org.clokey.member.entity.QMember.member;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.category.entity.QCategory;
import org.clokey.coordinate.enums.CoordinateType;
import org.clokey.domain.coordinate.dto.response.CoordinateDetailsListResponse;
import org.clokey.domain.coordinate.dto.response.DailyCoordinateListResponse;
import org.clokey.domain.lookbook.dto.response.CoordinateListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CoordinateRepositoryImpl implements CoordinateRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<DailyCoordinateListResponse> findAllDailyCoordinateByMemberId(
            Long currentMemberId, Long lastCoordinateId, int size, SortDirection direction) {

        List<DailyCoordinateListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        DailyCoordinateListResponse.class,
                                        coordinate.id,
                                        coordinate.imageUrl,
                                        coordinate.createdAt))
                        .from(coordinate)
                        .join(coordinate.member, member)
                        .where(
                                coordinate.member.id.eq(currentMemberId),
                                coordinate.coordinateType.eq(CoordinateType.DAILY),
                                coordinate.lookBook.isNull(),
                                lastCoordinateIdCondition(lastCoordinateId, direction))
                        .orderBy(
                                direction == SortDirection.DESC
                                        ? coordinate.id.desc()
                                        : coordinate.id.asc())
                        .limit(size + 1)
                        .fetch();

        return checkLastPage(size, results);
    }

    @Override
    public List<CoordinateDetailsListResponse> findAllCoordinateDetailsByCoordinateId(
            Long coordinateId) {

        QCategory parentCategory = new QCategory("parentCategory");

        return queryFactory
                .select(
                        Projections.constructor(
                                CoordinateDetailsListResponse.class,
                                coordinateCloth.id,
                                coordinateCloth.location.locationX,
                                coordinateCloth.location.locationY,
                                coordinateCloth.ratio,
                                coordinateCloth.degree,
                                coordinateCloth.order,
                                coordinateCloth.cloth.id,
                                coordinateCloth.cloth.clothImageUrl,
                                coordinateCloth.cloth.brand,
                                coordinateCloth.cloth.name,
                                category.name,
                                parentCategory.name))
                .from(coordinateCloth)
                .join(coordinateCloth.cloth, cloth)
                .join(cloth.category, category)
                .leftJoin(category.parent, parentCategory)
                .where(coordinateCloth.coordinate.id.eq(coordinateId))
                .orderBy(coordinateCloth.id.asc())
                .fetch();
    }

    @Override
    public Slice<CoordinateListResponse> findAllCoordinateByLookBookId(
            Long lookBookId, Long lastCoordinateId, int size, SortDirection direction) {
        List<CoordinateListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        CoordinateListResponse.class,
                                        coordinate.id,
                                        coordinate.name,
                                        coordinate.liked,
                                        coordinate.imageUrl))
                        .from(coordinate)
                        .where(
                                coordinate.lookBook.id.eq(lookBookId),
                                lastCoordinateIdCondition(lastCoordinateId, direction))
                        .orderBy(
                                direction == SortDirection.DESC
                                        ? coordinate.id.desc()
                                        : coordinate.id.asc())
                        .limit(size + 1)
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression lastCoordinateIdCondition(
            Long coordinateId, SortDirection direction) {
        if (coordinateId == null) {
            return null;
        }

        return direction == SortDirection.DESC
                ? coordinate.id.lt(coordinateId)
                : coordinate.id.gt(coordinateId);
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
