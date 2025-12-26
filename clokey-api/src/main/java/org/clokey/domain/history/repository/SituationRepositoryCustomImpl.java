package org.clokey.domain.history.repository;

import static org.clokey.history.entity.QSituation.situation;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.history.dto.response.SituationListResponse;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SituationRepositoryCustomImpl implements SituationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public SituationListResponse findAllSituations() {
        List<SituationListResponse.Content> contents =
                queryFactory
                        .select(
                                Projections.constructor(
                                        SituationListResponse.Content.class,
                                        situation.id,
                                        situation.name))
                        .from(situation)
                        .orderBy(situation.id.asc())
                        .fetch();

        return new SituationListResponse(contents);
    }
}
