package org.clokey.domain.coordinate.repository;

import java.util.List;
import org.clokey.domain.coordinate.dto.response.CoordinateDetailsListResponse;
import org.clokey.domain.coordinate.dto.response.DailyCoordinateListResponse;
import org.clokey.domain.lookbook.dto.response.CoordinateListResponse;
import org.clokey.global.paging.SortDirection;
import org.springframework.data.domain.Slice;

public interface CoordinateRepositoryCustom {
    Slice<DailyCoordinateListResponse> findAllDailyCoordinateByMemberId(
            Long currentMemberId, Long lastCoordinateId, int size, SortDirection direction);

    List<CoordinateDetailsListResponse> findAllCoordinateDetailsByCoordinateId(Long coordinateId);

    Slice<CoordinateListResponse> findAllCoordinateByLookBookId(
            Long lookBookId, Long lastCoordinateId, int size, SortDirection direction);
}
