package org.clokey.domain.coordinate.service;

import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.DailyCoordinateCreateResponse;

public interface CoordinateService {

    DailyCoordinateCreateResponse createDailyCoordinate(DailyCoordinateCreateRequest request);
}
