package org.clokey.report.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;
import org.clokey.history.entity.History;
import org.clokey.member.entity.Member;
import org.clokey.report.enums.HistoryReportType;
import org.clokey.report.enums.ReportStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private HistoryReportType historyReportType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReportStatus reportStatus;

    @Column(length = 200)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id")
    @NotNull
    private History history;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private HistoryReport(
    //            HistoryReportType historyReportType, History history, Member member, String
    // content) {
    //        this.historyReportType = historyReportType;
    //        this.history = history;
    //        this.member = member;
    //        this.content = content;
    //        this.reportStatus = ReportStatus.UNCHECKED; // 기본값 지정
    //    }
    //
    //    public static HistoryReport createHistoryReport(
    //            HistoryReportType historyReportType, History history, Member member, String
    // content) {
    //        return HistoryReport.builder()
    //                .historyReportType(historyReportType)
    //                .history(history)
    //                .member(member)
    //                .content(content)
    //                .build();
    //    }
}
