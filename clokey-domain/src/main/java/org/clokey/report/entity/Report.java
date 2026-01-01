package org.clokey.report.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;
import org.clokey.report.enums.ReportReason;
import org.clokey.report.enums.ReportStatus;
import org.clokey.report.enums.TargetType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_member_id")
    @NotNull
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_id")
    @NotNull
    private Member reported;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReportReason reportReason;

    @Column(length = 200)
    private String content;

    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private Report(
            Long targetId,
            Member reporter,
            Member reported,
            TargetType targetType,
            ReportReason reportReason,
            String content,
            ReportStatus reportStatus) {
        this.targetId = targetId;
        this.reporter = reporter;
        this.reported = reported;
        this.targetType = targetType;
        this.reportReason = reportReason;
        this.content = content;
        this.reportStatus = reportStatus;
    }

    public static Report createReport(
            Long targetId,
            Member reporter,
            Member reported,
            TargetType targetType,
            ReportReason reportReason,
            String content) {
        Report report =
                Report.builder()
                        .targetId(targetId)
                        .reporter(reporter)
                        .reported(reported)
                        .targetType(targetType)
                        .reportReason(reportReason)
                        .content(content)
                        .build();
        report.reportStatus = ReportStatus.UNCHECKED;
        return report;
    }

    public void updateReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }
}
