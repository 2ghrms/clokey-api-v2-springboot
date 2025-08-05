package org.clokey.report.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;
import org.clokey.report.enums.ProfileReportType;
import org.clokey.report.enums.ReportStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profile_report")
public class ProfileReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ProfileReportType profileReportType;

    @Column(length = 200)
    private String content;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReportStatus reportStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    @NotNull
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id")
    @NotNull
    private Member reported;

    //    @Builder(access = AccessLevel.PRIVATE)
    //    private ProfileReport(
    //            Member reporter, Member reported, ProfileReportType profileReportType, String
    // content) {
    //        this.reporter = reporter;
    //        this.reported = reported;
    //        this.profileReportType = profileReportType;
    //        this.content = content;
    //        this.reportStatus = ReportStatus.UNCHECKED; // 기본값 지정
    //    }
    //
    //    public static ProfileReport createProfileReport(
    //            Member reporter, Member reported, ProfileReportType profileReportType, String
    // content) {
    //        return ProfileReport.builder()
    //                .reporter(reporter)
    //                .reported(reported)
    //                .profileReportType(profileReportType)
    //                .content(content)
    //                .build();
    //    }
}
