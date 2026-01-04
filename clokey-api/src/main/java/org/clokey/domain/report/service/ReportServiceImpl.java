package org.clokey.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.report.dto.request.ReportCreateRequest;
import org.clokey.domain.report.dto.response.ReportCreateResponse;
import org.clokey.domain.report.dto.response.ReportedCheckResponse;
import org.clokey.domain.report.exception.ReportErrorCode;
import org.clokey.domain.report.repository.ReportRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.report.entity.Report;
import org.clokey.report.enums.ReportStatus;
import org.clokey.report.enums.TargetType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final MemberUtil memberUtil;

    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;
    private final HistoryRepository historyRepository;

    @Override
    @Transactional
    public ReportCreateResponse createReport(ReportCreateRequest request) {
        Member reporter = memberUtil.getCurrentMember();
        Member reported = getReportedMember(request.targetType(), request.targetId());
        validateDuplicateReport(request);

        Report report =
                Report.createReport(
                        request.targetId(),
                        reporter,
                        reported,
                        request.targetType(),
                        request.reportReason(),
                        request.content());

        reportRepository.save(report);

        return ReportCreateResponse.from(report);
    }

    @Override
    public ReportedCheckResponse checkReportReceived() {
        Member member = memberUtil.getCurrentMember();

        Report report =
                reportRepository
                        .findTopByReported_IdAndReportStatusOrderByCreatedAtDesc(
                                member.getId(), ReportStatus.UNCHECKED)
                        .orElse(null);

        if (report != null) {
            return ReportedCheckResponse.of(true, report.getTargetType());
        }

        return ReportedCheckResponse.of(false, null);
    }

    private Member getReportedMember(TargetType targetType, Long targetId) {
        if (targetType.equals(TargetType.COMMENT)) {
            return commentRepository
                    .findById(targetId)
                    .orElseThrow(() -> new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND))
                    .getMember();
        } else {
            return historyRepository
                    .findById(targetId)
                    .orElseThrow(() -> new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND))
                    .getMember();
        }
    }

    private void validateDuplicateReport(ReportCreateRequest request) {
        boolean exists =
                reportRepository.existsByTargetTypeAndTargetIdAndReportStatusIsNot(
                        request.targetType(), request.targetId(), ReportStatus.DISAPPROVED);

        if (exists) {
            throw new BaseCustomException(ReportErrorCode.REPORT_DUPLICATED);
        }
    }
}
