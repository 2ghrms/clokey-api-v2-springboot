package org.clokey.domain.report.repository;

import org.clokey.report.entity.HistoryReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryReportRepository extends JpaRepository<HistoryReport, Long> {}
