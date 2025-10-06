package org.clokey.domain.coordinate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.coordinate.dto.request.CoordinateAutoCreateRequest;
import org.clokey.domain.coordinate.dto.request.CoordinateManualCreateRequest;
import org.clokey.domain.coordinate.dto.request.DailyCoordinateCreateRequest;
import org.clokey.domain.coordinate.dto.response.CoordinateCreateResponse;
import org.clokey.domain.coordinate.service.CoordinateService;
import org.clokey.response.BaseResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coordinate")
@RequiredArgsConstructor
@Tag(name = "5. 코디 API", description = "코디 관련 API입니다.")
public class CoordinateController {

    private final CoordinateService coordinateService;

    @PostMapping("/daily")
    @Operation(summary = "오늘의 코디 생성", description = "홈화면에서 오늘의 코디를 생성하는 API입니다.")
    public BaseResponse<CoordinateCreateResponse> createDailyCoordinate(
            @Valid @RequestBody DailyCoordinateCreateRequest request) {
        CoordinateCreateResponse response = coordinateService.createDailyCoordinate(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/manual")
    @Operation(summary = "코디 수동 생성", description = "코디 생성을 수동으로 하는 API입니다.")
    public BaseResponse<CoordinateCreateResponse> createCoordinateManual(
            @Valid @RequestBody CoordinateManualCreateRequest request) {
        CoordinateCreateResponse response = coordinateService.createCoordinateManual(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/auto")
    @Operation(summary = "코디 자동 생성", description = "오늘의 코디를 통해서 코디를 생성하는 API입니다.")
    public BaseResponse<CoordinateCreateResponse> createCoordinateAuto(
            @Valid @RequestBody CoordinateAutoCreateRequest request) {
        CoordinateCreateResponse response = coordinateService.createCoordinateAuto(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }
}
