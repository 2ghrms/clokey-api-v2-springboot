package org.clokey.domain.history.repository;

import static org.clokey.history.entity.QStyle.style;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.history.dto.response.StyleListResponse;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StyleRepositoryCustomImpl implements StyleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public StyleListResponse findAllStyles() {
        List<StyleListResponse.Content> contents =
                queryFactory
                        .select(
                                Projections.constructor(
                                        StyleListResponse.Content.class, style.id, style.name))
                        .from(style)
                        .orderBy(style.id.asc())
                        .fetch();

        return new StyleListResponse(contents);
    }
}
