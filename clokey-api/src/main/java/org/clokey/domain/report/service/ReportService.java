package org.clokey.domain.report.service;

import org.clokey.domain.report.dto.request.ReportCreateRequest;
import org.clokey.domain.report.dto.response.ReportCreateResponse;
import org.clokey.domain.report.dto.response.ReportedCheckResponse;

public interface ReportService {

    ReportCreateResponse createReport(ReportCreateRequest request);

    ReportedCheckResponse checkReportReceived();
}
