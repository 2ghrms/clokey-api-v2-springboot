package org.clokey.domain.coordinate.service;

import org.clokey.domain.coordinate.dto.request.CoordinateAutoCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateManualCreateRequest;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.CoordinateCreateResponse;

public interface CoordinateService {

    CoordinateCreateResponse createDailyCoordinate(DailyCoordinateCreateRequest request);

    CoordinateCreateResponse createCoordinateManual(CoordinateManualCreateRequest request);

    CoordinateCreateResponse createCoordinateAuto(CoordinateAutoCreateRequest request);
}
