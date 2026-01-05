package org.clokey.domain.report.repository;

import java.util.Optional;
import org.clokey.report.entity.Report;
import org.clokey.report.enums.ReportStatus;
import org.clokey.report.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByTargetTypeAndTargetIdAndReportStatusIsNot(
            TargetType targetType, Long TargetId, ReportStatus reportStatus);

    Optional<Report> findTopByReported_IdAndReportStatusOrderByCreatedAtDesc(
            Long memberId, ReportStatus reportStatus);
}
