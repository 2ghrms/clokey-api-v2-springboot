package org.clokey.report.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.clokey.comment.entitiy.Comment;
import org.clokey.common.model.BaseEntity;
import org.clokey.member.entity.Member;
import org.clokey.report.enums.CommentReportType;
import org.clokey.report.enums.ReportStatus;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private CommentReportType commentReportType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReportStatus reportStatus;

    @Column(length = 200)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    @NotNull
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;
    //
    //    @Builder(access = AccessLevel.PRIVATE)
    //    private CommentReport(
    //            CommentReportType commentReportType, Comment comment, Member member, String
    // content) {
    //        this.commentReportType = commentReportType;
    //        this.comment = comment;
    //        this.member = member;
    //        this.content = content;
    //        this.reportStatus = ReportStatus.UNCHECKED; // 기본값
    //    }
    //
    //    public static CommentReport createCommentReport(
    //            CommentReportType commentReportType, Comment comment, Member member, String
    // content) {
    //        return CommentReport.builder()
    //                .commentReportType(commentReportType)
    //                .comment(comment)
    //                .member(member)
    //                .content(content)
    //                .build();
    //    }
}
