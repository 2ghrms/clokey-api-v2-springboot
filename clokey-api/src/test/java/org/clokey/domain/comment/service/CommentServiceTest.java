package org.clokey.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import org.clokey.IntegrationTest;
import org.clokey.comment.entitiy.Comment;
import org.clokey.comment.entitiy.Reply;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.request.ReplyCreateRequest;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.comment.repository.ReplyRepository;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.HistoryTypeRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.FakeAuthContext;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryType;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.OauthProvider;
import org.clokey.member.enums.RegisterStatus;
import org.clokey.member.enums.Visibility;
import org.clokey.util.TransactionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CommentServiceTest extends IntegrationTest {

    @Autowired TransactionUtil transactionUtil;

    @Autowired CommentService commentService;
    @Autowired CommentRepository commentRepository;
    @Autowired ReplyRepository replyRepository;
    @Autowired HistoryRepository historyRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired HistoryTypeRepository historyTypeRepository;

    @MockitoBean FakeAuthContext fakeAuthContext;

    @Nested
    class 댓글을_작성할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO),
                            MemberStatus.ACTIVE,
                            RegisterStatus.REGISTERED,
                            Visibility.PUBLIC);
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO),
                            MemberStatus.ACTIVE,
                            RegisterStatus.REGISTERED,
                            Visibility.PRIVATE);

            memberRepository.saveAll(List.of(member1, member2));
            given(fakeAuthContext.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history1 =
                    History.creatHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, historyType);
            History history2 =
                    History.creatHistory(
                            LocalDate.of(2025, 1, 1), "testContent2", member2, historyType);
            historyRepository.saveAll(List.of(history1, history2));
        }

        @Test
        void 유효한_요청이면_댓글을_생성한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");

            // when
            commentService.createComment(request);

            // then
            Comment comment = commentRepository.findById(1L).orElseThrow();
            assertThat(comment)
                    .extracting("content", "banned", "member.id", "history.id")
                    .containsExactly("testContent", false, 1L, 1L);
        }

        @Test
        void 기록이_존재하지_않는_경우_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(999L, "testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createComment(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }

        @Test
        void 내가_아닌_비공개_계정의_기록에_댓글을_작성하면_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(2L, "testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createComment(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }

        @Test
        void 댓글_작성_중_히스토리가_삭제되면_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "댓글 내용");

            var barrierBeforeDelete = new CyclicBarrier(2);
            var barrierAfterDelete = new CyclicBarrier(2);
            var es = Executors.newFixedThreadPool(2);

            // when & then
            var f1 =
                    es.submit(
                            () -> {
                                barrierBeforeDelete.await();
                                historyRepository.deleteById(1L);
                                barrierAfterDelete.await();

                                return null;
                            });

            var f2 =
                    es.submit(
                            () -> {
                                transactionUtil.getResult(
                                        () -> {
                                            historyRepository
                                                    .findById(1L)
                                                    .orElseThrow(
                                                            () ->
                                                                    new BaseCustomException(
                                                                            HistoryErrorCode
                                                                                    .HISTORY_NOT_FOUND));

                                            try {
                                                barrierBeforeDelete.await();
                                                barrierAfterDelete.await();
                                            } catch (InterruptedException
                                                    | BrokenBarrierException e) {
                                                throw new RuntimeException(e);
                                            }

                                            assertThatThrownBy(
                                                            () ->
                                                                    commentService.createComment(
                                                                            request))
                                                    .isInstanceOf(BaseCustomException.class)
                                                    .hasMessage(
                                                            HistoryErrorCode.HISTORY_NOT_FOUND
                                                                    .getMessage());

                                            return null;
                                        });
                                return null;
                            });
        }
    }

    @Nested
    class 대댓글을_작성할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO),
                            MemberStatus.ACTIVE,
                            RegisterStatus.REGISTERED,
                            Visibility.PUBLIC);
            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO),
                            MemberStatus.ACTIVE,
                            RegisterStatus.REGISTERED,
                            Visibility.PRIVATE);

            memberRepository.saveAll(List.of(member1, member2));
            given(fakeAuthContext.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history1 =
                    History.creatHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, historyType);
            History history2 =
                    History.creatHistory(
                            LocalDate.of(2025, 1, 1), "testContent2", member2, historyType);
            historyRepository.saveAll(List.of(history1, history2));

            Comment comment1 = Comment.createComment("testContent1", member1, history1);
            Comment comment2 = Comment.createComment("testContent2", member2, history2);
            commentRepository.saveAll(List.of(comment1, comment2));
        }

        @Test
        void 유효한_요청이면_대댓글을_생성한다() {
            // given
            ReplyCreateRequest request = new ReplyCreateRequest("testContent");

            // when
            commentService.createReply(1L, request);

            // then
            Reply reply = replyRepository.findById(1L).orElseThrow();
            assertThat(reply)
                    .extracting("content", "banned", "member.id", "comment.id")
                    .containsExactly("testContent", false, 1L, 1L);
        }

        @Test
        void 댓글이_존재하지_않는_경우_예외가_발생한다() {
            // given
            ReplyCreateRequest request = new ReplyCreateRequest("testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(999L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 내가_아닌_비공개_계정의_기록에_작성된_댓글에_대댓글을_작성하면_예외가_발생한다() {
            // given
            ReplyCreateRequest request = new ReplyCreateRequest("testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(2L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }

        @Test
        void 대댓글을_작성하려는_댓글이_삭제되는_동시성_문제가_발생하면_예외가_발생한다() {

            // given
            ReplyCreateRequest request = new ReplyCreateRequest("testReplyContent");
            var barrierBeforeDelete = new CyclicBarrier(2);
            var barrierAfterDelete = new CyclicBarrier(2);
            var es = Executors.newFixedThreadPool(2);

            // when & then
            var f1 =
                    es.submit(
                            () -> {
                                barrierBeforeDelete.await();
                                commentRepository.deleteById(1L);
                                barrierAfterDelete.await();
                                return null;
                            });

            var f2 =
                    es.submit(
                            () -> {
                                transactionUtil.getResult(
                                        () -> {
                                            Comment comment =
                                                    commentRepository.findById(1L).orElseThrow();

                                            try {
                                                barrierBeforeDelete.await();
                                                barrierAfterDelete.await();
                                            } catch (InterruptedException
                                                    | BrokenBarrierException e) {
                                                throw new RuntimeException(e);
                                            }

                                            assertThatThrownBy(
                                                            () ->
                                                                    commentService.createReply(
                                                                            1L, request))
                                                    .isInstanceOf(BaseCustomException.class)
                                                    .hasMessage(
                                                            CommentErrorCode.COMMENT_NOT_FOUND
                                                                    .getMessage());

                                            return null;
                                        });
                                return null;
                            });
        }
    }
}
