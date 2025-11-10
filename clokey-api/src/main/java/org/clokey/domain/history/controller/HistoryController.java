package org.clokey.domain.history.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.history.dto.request.HistoryCreateRequest;
import org.clokey.domain.history.dto.response.HistoryCreateResponse;
import org.clokey.domain.history.service.HistoryService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/histories")
@RequiredArgsConstructor
@Tag(name = "7. 기록 API", description = "기록 관련 API입니다.")
@Validated
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping
    @Operation(summary = "기록 생성", description = "새로운 기록을 생성합니다.")
    public BaseResponse<HistoryCreateResponse> createHistory(
            @Valid @RequestBody HistoryCreateRequest request) {
        HistoryCreateResponse response = historyService.createHistory(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }
}
