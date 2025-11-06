package org.clokey.domain.coordinate.service;

import java.util.List;
import org.clokey.domain.coordinate.dto.request.CoordinateAutoCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateManualCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateUpdateRequest;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.*;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface CoordinateService {

    CoordinateCreateResponse createDailyCoordinate(DailyCoordinateCreateRequest request);

    CoordinateCreateResponse createCoordinateManual(CoordinateManualCreateRequest request);

    CoordinateCreateResponse createCoordinateAuto(CoordinateAutoCreateRequest request);

    void updateCoordinate(Long coordinateId, CoordinateUpdateRequest request);

    void deleteCoordinate(Long coordinateId);

    SliceResponse<DailyCoordinateListResponse> getDailyCoordinates(
            Long lastCoordinateId, int size, SortDirection direction);

    CoordinatePreviewResponse getCoordinatePreview(Long coordinateId);

    List<CoordinateDetailsListResponse> getCoordinateDetails(Long coordinateId);

    void toggleCoordinateLike(Long coordinateId);

    List<FavoriteCoordinateResponse> getFavoriteCoordinates();
}
