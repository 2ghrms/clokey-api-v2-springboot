package org.clokey.domain.history.repository;

import org.clokey.domain.history.dto.response.SituationListResponse;

public interface SituationRepositoryCustom {
    SituationListResponse findAllSituations();
}
