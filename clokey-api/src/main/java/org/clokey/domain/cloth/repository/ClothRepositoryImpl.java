package org.clokey.domain.cloth.repository;

import static org.clokey.cloth.entity.QCloth.cloth;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.entity.Cloth;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.domain.cloth.dto.response.ClothRecommendListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ClothRepositoryImpl implements ClothRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ClothRecommendListResponse> findAllMemberRecommendClothesByCategoryAndSeason(
            Long lastClothId, int size, Long categoryId, Long memberId, Season season) {

        Season nextSeason = season.next();
        Season previousSeason = season.previous();
        Season oppositeSeason = season.next().next();

        // 우선 순위에 맞게 페이징 합니다.
        // - Category는 고정입니다.
        // - 계절은 요청한 계절에 가까운 순서대로 페이징을 진행합니다.
        NumberExpression<Integer> seasonPriority =
                new CaseBuilder()
                        .when(cloth.season.eq(season))
                        .then(1)
                        .when(cloth.season.in(nextSeason, previousSeason))
                        .then(2)
                        .when(cloth.season.eq(oppositeSeason))
                        .then(3)
                        .otherwise(4);

        List<ClothRecommendListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        ClothRecommendListResponse.class,
                                        cloth.id,
                                        cloth.clothImageUrl))
                        .from(cloth)
                        .where(
                                cloth.category.id.eq(categoryId),
                                cloth.member.id.eq(memberId),
                                cloth.season.in(season, nextSeason, previousSeason, oppositeSeason),
                                lastClothIdCondition(
                                        lastClothId,
                                        season,
                                        nextSeason,
                                        previousSeason,
                                        oppositeSeason,
                                        seasonPriority))
                        .orderBy(seasonPriority.asc(), cloth.id.asc())
                        .limit((long) size + 1)
                        .fetch();

        return checkLastPage(size, results);
    }

    @Override
    public Slice<ClothListResponse> findAllMemberClothesByCategoriesAndSeasons(
            Long lastClothId,
            int size,
            SortDirection direction,
            List<Long> categoryIds,
            Long memberId,
            List<Season> seasons) {

        List<ClothListResponse> results =
                queryFactory
                        .select(
                                Projections.constructor(
                                        ClothListResponse.class,
                                        cloth.id,
                                        cloth.clothImageUrl,
                                        cloth.brand,
                                        cloth.name))
                        .from(cloth)
                        .where(
                                cloth.member.id.eq(memberId),
                                categoryFilter(categoryIds),
                                seasonsFilter(seasons),
                                lastClothIdCursor(lastClothId, direction))
                        .orderBy(direction == SortDirection.DESC ? cloth.id.desc() : cloth.id.asc())
                        .limit((long) size + 1)
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression categoryFilter(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }
        return cloth.category.id.in(categoryIds);
    }

    private BooleanExpression seasonsFilter(List<Season> seasons) {
        if (seasons == null || seasons.isEmpty()) {
            return null;
        }
        return cloth.season.in(seasons);
    }

    private BooleanExpression lastClothIdCursor(Long lastClothId, SortDirection direction) {
        if (lastClothId == null) {
            return null;
        }
        return direction == SortDirection.DESC
                ? cloth.id.lt(lastClothId)
                : cloth.id.gt(lastClothId);
    }

    private BooleanExpression lastClothIdCondition(
            Long lastClothId,
            Season season,
            Season nextSeason,
            Season previousSeason,
            Season oppositeSeason,
            NumberExpression<Integer> seasonPriority) {
        if (lastClothId == null) {
            return null;
        }

        Cloth lastCloth = queryFactory.selectFrom(cloth).where(cloth.id.eq(lastClothId)).fetchOne();

        if (lastCloth == null) {
            return null;
        }

        int lastPriority =
                calculateSeasonPriority(
                        lastCloth.getSeason(), season, nextSeason, previousSeason, oppositeSeason);

        // 복합 조건: (우선순위 > 마지막 우선순위) OR (우선순위 = 마지막 우선순위 AND ID > 마지막 ID)
        return seasonPriority
                .gt(lastPriority)
                .or(seasonPriority.eq(lastPriority).and(cloth.id.gt(lastClothId)));
    }

    private int calculateSeasonPriority(
            Season clothSeason,
            Season season,
            Season nextSeason,
            Season previousSeason,
            Season oppositeSeason) {
        if (clothSeason == season) {
            return 1;
        } else if (clothSeason == nextSeason || clothSeason == previousSeason) {
            return 2;
        } else if (clothSeason == oppositeSeason) {
            return 3;
        }
        return 4;
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
