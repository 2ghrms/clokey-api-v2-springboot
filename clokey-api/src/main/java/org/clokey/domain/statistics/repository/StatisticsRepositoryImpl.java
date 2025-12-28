package org.clokey.domain.statistics.repository;

import static org.clokey.cloth.entity.QCloth.cloth;
import static org.clokey.coordinate.entity.QCoordinate.coordinate;
import static org.clokey.coordinate.entity.QCoordinateCloth.coordinateCloth;
import static org.clokey.history.entity.QHistoryClothTag.historyClothTag;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.enums.Season;
import org.clokey.coordinate.enums.CoordinateType;
import org.clokey.domain.statistics.dto.CategoryCountDto;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StatisticsRepositoryImpl implements StatisticsRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CategoryCountDto> countClothesByChildCategories(
            Long memberId, Long parentCategoryId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                CategoryCountDto.class,
                                cloth.category.id,
                                cloth.category.name,
                                cloth.id.count()))
                .from(cloth)
                .where(cloth.member.id.eq(memberId), cloth.category.parent.id.eq(parentCategoryId))
                .groupBy(cloth.category)
                .orderBy(cloth.id.count().desc())
                .fetch();
    }

    @Override
    public List<CategoryCountDto> countClothesByCategoriesTopN(Long memberId, int limit) {
        return queryFactory
                .select(
                        Projections.constructor(
                                CategoryCountDto.class,
                                cloth.category.id,
                                cloth.category.name,
                                cloth.id.count()))
                .from(cloth)
                .where(cloth.member.id.eq(memberId))
                .groupBy(cloth.category)
                .orderBy(cloth.id.count().desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long countClothesBySeason(Long memberId, Season season) {
        return queryFactory
                .select(cloth.id.count())
                .from(cloth)
                .where(cloth.member.id.eq(memberId), cloth.season.eq(season))
                .fetchOne();
    }

    @Override
    public Set<Long> findUtilizedClothIds(Long memberId, Season season) {
        Set<Long> utilizedClothIds = new HashSet<>();

        // HistoryClothTag에 태그된 옷 ID들
        List<Long> historyClothIds =
                queryFactory
                        .select(historyClothTag.cloth.id)
                        .from(historyClothTag)
                        .where(
                                historyClothTag.cloth.member.id.eq(memberId),
                                historyClothTag.cloth.season.eq(season))
                        .distinct()
                        .fetch();
        utilizedClothIds.addAll(historyClothIds);

        // Coordinate (DAILY) -> CoordinateCloth에 존재하는 옷 ID들
        List<Long> coordinateClothIds =
                queryFactory
                        .select(coordinateCloth.cloth.id)
                        .from(coordinateCloth)
                        .join(coordinateCloth.coordinate, coordinate)
                        .where(
                                coordinate.member.id.eq(memberId),
                                coordinate.coordinateType.eq(CoordinateType.DAILY),
                                coordinateCloth.cloth.member.id.eq(memberId),
                                coordinateCloth.cloth.season.eq(season))
                        .distinct()
                        .fetch();
        utilizedClothIds.addAll(coordinateClothIds);

        return utilizedClothIds;
    }

    @Override
    public List<org.clokey.cloth.entity.Cloth> findAllClothesBySeason(
            Long memberId, Season season) {
        return queryFactory
                .selectFrom(cloth)
                .where(cloth.member.id.eq(memberId), cloth.season.eq(season))
                .fetch();
    }
}
