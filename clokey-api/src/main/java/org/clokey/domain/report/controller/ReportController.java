package org.clokey.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.report.dto.request.ReportCreateRequest;
import org.clokey.domain.report.dto.response.ReportCreateResponse;
import org.clokey.domain.report.dto.response.ReportedCheckResponse;
import org.clokey.domain.report.service.ReportService;
import org.clokey.response.BaseResponse;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/reports")
@Tag(name = "13. 신고 API", description = "댓글 및 기록 신고 API입니다.")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "신고 생성", description = "신고를 생성합니다.")
    public BaseResponse<ReportCreateResponse> createNewReport(
            @Valid @RequestBody ReportCreateRequest reportCreatRequest) {
        ReportCreateResponse response = reportService.createReport(reportCreatRequest);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @GetMapping("/received")
    @Operation(
            operationId = "Report_checkReportReceived",
            summary = "사용자에 접수된 신고 확인",
            description = "사용자에 접수된 신고(미확인 상태)가 있는지 확인합니다.")
    public BaseResponse<ReportedCheckResponse> checkReportReceived() {
        ReportedCheckResponse response = reportService.checkReportReceived();

        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
}
