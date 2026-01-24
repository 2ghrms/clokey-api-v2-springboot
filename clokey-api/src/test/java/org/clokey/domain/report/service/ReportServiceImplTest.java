package org.clokey.domain.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.comment.entitiy.Comment;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.SituationRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.domain.report.dto.request.ReportCreateRequest;
import org.clokey.domain.report.dto.response.ReportedCheckResponse;
import org.clokey.domain.report.exception.ReportErrorCode;
import org.clokey.domain.report.repository.ReportRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.Situation;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.report.entity.Report;
import org.clokey.report.enums.ReportReason;
import org.clokey.report.enums.ReportStatus;
import org.clokey.report.enums.TargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

public class ReportServiceImplTest extends IntegrationTest {

    @Autowired ReportService reportService;
    @MockitoSpyBean ReportRepository reportRepository;
    @Autowired CommentRepository commentRepository;
    @Autowired HistoryRepository historyRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired SituationRepository situationRepository;

    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 신고를_생성할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            memberRepository.save(member1);
            given(memberUtil.getCurrentMember()).willReturn(member1);

            Situation situation = Situation.createSituation("testSituation");
            situationRepository.save(situation);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, situation);
            historyRepository.save(history1);

            Comment comment1 = Comment.createParentComment("옷이 예쁘네요", member1, history1);
            commentRepository.save(comment1);
        }

        @Test
        void 유효한_요청이면_신고를_생성한다() {
            // given
            ReportCreateRequest request =
                    new ReportCreateRequest(
                            1L, TargetType.HISTORY, ReportReason.VIOLENT, "사진이 혐짤이에요.. ㅠㅠ");

            // when
            reportService.createReport(request);

            // then
            Report report = reportRepository.findById(1L).orElseThrow();
            assertThat(report)
                    .extracting(
                            "targetId",
                            "reporter.id",
                            "reported.id",
                            "targetType",
                            "reportReason",
                            "reportStatus",
                            "content")
                    .containsExactly(
                            1L,
                            1L,
                            1L,
                            TargetType.HISTORY,
                            ReportReason.VIOLENT,
                            ReportStatus.UNCHECKED,
                            "사진이 혐짤이에요.. ㅠㅠ");
        }

        @Test
        void 반려된_신고에_대한_재신고가_들어왔을_때() {
            // given
            ReportCreateRequest request1 =
                    new ReportCreateRequest(
                            1L, TargetType.HISTORY, ReportReason.VIOLENT, "혐짤을 올려놨어요");
            reportService.createReport(request1);
            Report disapprovedReport = reportRepository.findById(1L).orElseThrow();
            disapprovedReport.updateReportStatus(ReportStatus.DISAPPROVED);
            reportRepository.save(disapprovedReport);

            ReportCreateRequest request2 =
                    new ReportCreateRequest(
                            1L, TargetType.HISTORY, ReportReason.VIOLENT, "이건 재신고 입니다.");

            // when
            reportService.createReport(request2);

            // then
            Report report = reportRepository.findById(2L).orElseThrow();
            assertThat(report)
                    .extracting(
                            "targetId",
                            "reporter.id",
                            "targetType",
                            "reportReason",
                            "reportStatus",
                            "content")
                    .containsExactly(
                            1L,
                            1L,
                            TargetType.HISTORY,
                            ReportReason.VIOLENT,
                            ReportStatus.UNCHECKED,
                            "이건 재신고 입니다.");
        }

        @Test
        void 댓글이_존재하지_않는_경우_예외가_발생한다() {
            // given
            ReportCreateRequest request =
                    new ReportCreateRequest(
                            1001L,
                            TargetType.COMMENT,
                            ReportReason.SWEARING_AND_CURSING,
                            "나한테 욕했어요");

            // when & then
            assertThatThrownBy(() -> reportService.createReport(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 기록이_존재하지_않는_경우_예외가_발생한다() {
            // given
            ReportCreateRequest request =
                    new ReportCreateRequest(
                            1001L, TargetType.HISTORY, ReportReason.SEXUAL, "선정적이에요");

            // when & then
            assertThatThrownBy(() -> reportService.createReport(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }

        @Test
        void 해당_콘텐츠에_이미_신고가_접수되었을_경우_예외가_발생한다() {
            // given
            ReportCreateRequest request1 =
                    new ReportCreateRequest(1L, TargetType.HISTORY, ReportReason.SEXUAL, "선정적이에요");
            reportService.createReport(request1);
            ReportCreateRequest request2 =
                    new ReportCreateRequest(
                            1L, TargetType.HISTORY, ReportReason.SEXUAL, "이건 또다른 신고예요.");

            // when & then
            assertThatThrownBy(() -> reportService.createReport(request2))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(ReportErrorCode.REPORT_DUPLICATED.getMessage());
        }
    }

    @Nested
    class 접수된_미확인_신고_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member2);

            Situation situation = Situation.createSituation("testSituation");
            situationRepository.save(situation);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2026, 1, 1), "testContent1", member2, situation);
            historyRepository.save(history1);

            Report report =
                    Report.createReport(
                            1L,
                            member1,
                            member2,
                            TargetType.HISTORY,
                            ReportReason.VIOLENT,
                            "Test Report");
            reportRepository.save(report);
        }

        @Test
        void 유효한_요청이면_미확인_신고_여부를_반환한다() {
            // when & then
            ReportedCheckResponse response = reportService.checkReportReceived();

            assertThat(response.isReported()).isTrue();
            assertThat(response.targetType()).isEqualTo(TargetType.HISTORY);
        }

        @Test
        void 접수된_신고가_없으면_false를_반환한다() {
            // given
            Member member1 = memberRepository.findById(1L).orElse(null);
            given(memberUtil.getCurrentMember()).willReturn(member1);

            // when & then
            ReportedCheckResponse response = reportService.checkReportReceived();

            assertThat(response.isReported()).isFalse();
            assertThat(response.targetType()).isEqualTo(null);
        }
    }
}
