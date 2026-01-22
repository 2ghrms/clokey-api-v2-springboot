package org.clokey.domain.search.repository;

import static org.clokey.cloth.entity.QCloth.cloth;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.cloth.enums.Season;
import org.clokey.domain.cloth.dto.response.ClothListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryCustomImpl implements SearchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ClothListResponse> findClothesByKeyword(
            String keyword,
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
                                        cloth.name,
                                        cloth.category.name))
                        .from(cloth)
                        .where(
                                cloth.member.id.eq(memberId),
                                keywordFilter(keyword),
                                categoryFilter(categoryIds),
                                seasonsFilter(seasons),
                                lastClothIdCursor(lastClothId, direction))
                        .orderBy(direction == SortDirection.DESC ? cloth.id.desc() : cloth.id.asc())
                        .limit((long) size + 1)
                        .fetch();

        return checkLastPage(size, results);
    }

    private BooleanExpression keywordFilter(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return cloth.name.contains(keyword).or(cloth.brand.contains(keyword));
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

    private <T> Slice<T> checkLastPage(int pageSize, List<T> results) {
        boolean hasNext = false;

        if (results.size() > pageSize) {
            hasNext = true;
            results.remove(pageSize);
        }

        return new SliceImpl<>(results, PageRequest.of(0, pageSize), hasNext);
    }
}
