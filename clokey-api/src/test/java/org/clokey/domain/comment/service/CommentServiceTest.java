package org.clokey.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.TransactionUtil;
import org.clokey.comment.entitiy.Comment;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.MyCommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.repository.CommentRepository;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.domain.history.repository.HistoryImageRepository;
import org.clokey.domain.history.repository.HistoryRepository;
import org.clokey.domain.history.repository.HistoryTypeRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.global.util.MemberUtil;
import org.clokey.history.entity.History;
import org.clokey.history.entity.HistoryImage;
import org.clokey.history.entity.HistoryType;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.OauthProvider;
import org.clokey.member.enums.Visibility;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

class CommentServiceTest extends IntegrationTest {

    @Autowired TransactionUtil transactionUtil;

    @Autowired CommentService commentService;
    @MockitoSpyBean CommentRepository commentRepository;
    @Autowired HistoryRepository historyRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired HistoryTypeRepository historyTypeRepository;
    @Autowired HistoryImageRepository historyImageRepository;

    @MockitoBean private MemberUtil memberUtil;

    @Nested
    class 댓글을_작성할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            member2.changeVisibility();
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, historyType);
            History history2 =
                    History.createHistory(
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
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");

            doAnswer(
                            invocation -> {
                                var sqlEx =
                                        new SQLIntegrityConstraintViolationException(
                                                "Cannot add or update a child row: a foreign key constraint fails "
                                                        + "(`testdb`.`comment`, CONSTRAINT `fk_comment_history` FOREIGN KEY (`history_id`) "
                                                        + "REFERENCES `history` (`id`))",
                                                "23000",
                                                1452);
                                throw new DataIntegrityViolationException(
                                        "constraint violation", sqlEx);
                            })
                    .when(commentRepository)
                    .save(any(Comment.class));

            // when & then
            assertThatThrownBy(() -> commentService.createComment(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
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
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            member2.changeVisibility();
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, historyType);
            History history2 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent2", member2, historyType);
            historyRepository.saveAll(List.of(history1, history2));

            Comment comment1 = Comment.createParentComment("testContent1", member1, history1);
            Comment comment2 = Comment.createParentComment("testContent2", member2, history2);
            Comment reply = Comment.createReply("testReply", member1, history1, comment1);
            commentRepository.saveAll(List.of(comment1, comment2, reply));
        }

        @Test
        void 유효한_요청이면_대댓글을_생성한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");

            // when
            commentService.createReply(1L, request);

            // then
            Comment reply = commentRepository.findById(3L).orElseThrow();
            assertThat(reply)
                    .extracting("content", "banned", "member.id", "comment.id", "history.id")
                    .containsExactly("testReply", false, 1L, 1L, 1L);
        }

        @Test
        void 댓글이_존재하지_않는_경우_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(999L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 기록이_존재하지_않는_경우_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(999L, "testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }

        @Test
        void 내가_아닌_비공개_계정의_기록에_작성된_댓글에_대댓글을_작성하면_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(2L, "testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(2L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }

        @Test
        void 대댓글의_부모_댓글의_기록_ID와_입력한_기록의_ID가_일치하지_않으면_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(2L, "testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(
                            CommentErrorCode.REPLY_HISTORY_PARENT_HISTORY_MISMATCH.getMessage());
        }

        @Test
        void 대댓글에_대댓글을_작성하는_경우_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");

            // when & then
            assertThatThrownBy(() -> commentService.createReply(3L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.REPLY_ON_REPLY.getMessage());
        }

        @Test
        void 대댓글을_작성하려는_댓글이_삭제되는_동시성_문제가_발생하면_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testReplyContent");

            doAnswer(
                            invocation -> {
                                var sqlEx =
                                        new SQLIntegrityConstraintViolationException(
                                                "Cannot add or update a child row: a foreign key constraint fails "
                                                        + "(`testdb`.`comment`, CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) "
                                                        + "REFERENCES `comment` (`id`))",
                                                "23000",
                                                1452);
                                throw new DataIntegrityViolationException(
                                        "constraint violation", sqlEx);
                            })
                    .when(commentRepository)
                    .save(any(Comment.class));

            // when & then
            assertThatThrownBy(() -> commentService.createReply(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 대댓글을_작성하려는_기록이_삭제되는_동시성_문제가_발생하면_예외가_발생한다() {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testReplyContent");

            doAnswer(
                            invocation -> {
                                var sqlEx =
                                        new SQLIntegrityConstraintViolationException(
                                                "Cannot add or update a child row: a foreign key constraint fails "
                                                        + "(`testdb`.`comment`, CONSTRAINT `fk_comment_history` FOREIGN KEY (`history_id`) "
                                                        + "REFERENCES `history` (`id`))",
                                                "23000",
                                                1452);
                                throw new DataIntegrityViolationException(
                                        "constraint violation", sqlEx);
                            })
                    .when(commentRepository)
                    .save(any(Comment.class));

            // when & then
            assertThatThrownBy(() -> commentService.createReply(1L, request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 기록의_댓글_목록을_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            member2.changeVisibility();
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, historyType);
            History history2 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent2", member2, historyType);
            History history3 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 2), "testContent3", member1, historyType);
            historyRepository.saveAll(List.of(history1, history2, history3));

            Comment comment1 = Comment.createParentComment("testContent1", member1, history1);
            Comment comment2 = Comment.createParentComment("testContent2", member2, history1);
            Comment comment3 = Comment.createParentComment("testContent3", member2, history1);
            commentRepository.saveAll(List.of(comment1, comment2, comment3));
        }

        @Test
        void 정렬_조건이_ASC이면_commentId를_오름차순으로_조회한다() {
            // when
            SliceResponse<CommentListResponse> response =
                    commentService.getHistoryComments(1L, null, 3, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("commentId").containsExactly(1L, 2L, 3L);
        }

        @Test
        void 정렬_조건이_DESC면_commentId를_내림차순으로_조회한다() {
            // when
            SliceResponse<CommentListResponse> response =
                    commentService.getHistoryComments(1L, null, 3, SortDirection.DESC);

            // then
            assertThat(response.content()).extracting("commentId").containsExactly(3L, 2L, 1L);
        }

        @Test
        void lastCommentId를_입력하면_다음_comment_부터_조회한다() {
            // when
            SliceResponse<CommentListResponse> response =
                    commentService.getHistoryComments(1L, 1L, 2, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("commentId").containsExactly(2L, 3L);
        }

        @Test
        void 기록에_댓글이_없는_경우_빈_리스트를_조회한다() {
            // when
            SliceResponse<CommentListResponse> response =
                    commentService.getHistoryComments(3L, null, 3, SortDirection.ASC);

            // when & then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isZero(),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_반환한다() {
            // when
            SliceResponse<CommentListResponse> response =
                    commentService.getHistoryComments(1L, null, 3, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(3),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_반환한다() {
            // when
            SliceResponse<CommentListResponse> response =
                    commentService.getHistoryComments(1L, null, 2, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(2),
                    () -> assertThat(response.isLast()).isFalse());
        }

        @Test
        void 존재하지_않는_기록을_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    commentService.getHistoryComments(
                                            999L, null, 3, SortDirection.ASC))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.HISTORY_NOT_FOUND.getMessage());
        }

        @Test
        void 내가_아닌_비공개_계정의_기록의_댓글을_조회하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(
                            () -> commentService.getHistoryComments(2L, null, 3, SortDirection.ASC))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }
    }

    @Nested
    class 댓글의_대댓글_목록을_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            member2.changeVisibility();
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, historyType);
            History history2 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent2", member2, historyType);
            historyRepository.saveAll(List.of(history1, history2));

            Comment comment1 = Comment.createParentComment("testContent1", member1, history1);
            Comment comment2 = Comment.createParentComment("testContent2", member2, history2);
            Comment comment3 = Comment.createParentComment("testContent3", member1, history1);
            Comment reply1 = Comment.createReply("testContent1", member1, history1, comment1);
            Comment reply2 = Comment.createReply("testContent2", member2, history1, comment1);
            Comment reply3 = Comment.createReply("testContent3", member2, history2, comment2);
            commentRepository.saveAll(
                    List.of(comment1, comment2, comment3, reply1, reply2, reply3));
        }

        @Test
        void 정렬_조건이_ASC이면_commnetId를_오름차순으로_조회한다() {
            // when
            SliceResponse<ReplyListResponse> response =
                    commentService.getCommentReplies(1L, null, 2, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("replyId").containsExactly(4L, 5L);
        }

        @Test
        void 정렬_조건이_DESC면_commentId를_내림차순으로_조회한다() {
            // when
            SliceResponse<ReplyListResponse> response =
                    commentService.getCommentReplies(1L, null, 2, SortDirection.DESC);

            // then
            assertThat(response.content()).extracting("replyId").containsExactly(5L, 4L);
        }

        @Test
        void lastReplyId를_입력하면_다음_comment_부터_조회한다() {
            // when
            SliceResponse<ReplyListResponse> response =
                    commentService.getCommentReplies(1L, 4L, 2, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("replyId").containsExactly(5L);
        }

        @Test
        void 댓글에_대댓글이_없는_경우_빈_리스트를_조회한다() {
            // when
            SliceResponse<ReplyListResponse> response =
                    commentService.getCommentReplies(3L, null, 3, SortDirection.ASC);

            // when & then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isZero(),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_반환한다() {
            // when
            SliceResponse<ReplyListResponse> response =
                    commentService.getCommentReplies(1L, null, 2, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(2),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_반환한다() {
            // when
            SliceResponse<ReplyListResponse> response =
                    commentService.getCommentReplies(1L, null, 1, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(1),
                    () -> assertThat(response.isLast()).isFalse());
        }

        @Test
        void 존재하지_않는_댓글을_입력하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    commentService.getCommentReplies(
                                            999L, null, 3, SortDirection.ASC))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 내가_아닌_비공개_계정의_기록의_대댓글을_조회하면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(
                            () -> commentService.getCommentReplies(2L, null, 3, SortDirection.ASC))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(HistoryErrorCode.LIMITED_AUTHORITY.getMessage());
        }
    }

    @Nested
    class 댓글을_삭제할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail1",
                            "testClokeyId1",
                            "testNickName1",
                            OauthInfo.createOauthInfo("testOauthId1", OauthProvider.KAKAO));

            Member member2 =
                    Member.createMember(
                            "testEmail2",
                            "testClokeyId2",
                            "testNickName2",
                            OauthInfo.createOauthInfo("testOauthId2", OauthProvider.KAKAO));
            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent", member1, historyType);
            historyRepository.save(history);

            Comment comment1 = Comment.createParentComment("testContent1", member1, history);
            Comment comment2 = Comment.createParentComment("testContent2", member1, history);
            Comment reply1 = Comment.createReply("testContent1", member1, history, comment1);
            Comment reply2 = Comment.createReply("testContent2", member1, history, comment1);
            Comment reply3 = Comment.createReply("testContent3", member1, history, comment2);
            commentRepository.saveAll(List.of(comment1, comment2, reply1, reply2, reply3));
        }

        @Test
        void 부모_댓글을_삭제하는_경우_대댓글도_모두_삭제된다() {
            // when
            commentService.deleteComment(1L);

            // then
            Assertions.assertAll(
                    () -> assertThat(commentRepository.findById(1L).isPresent()).isFalse(),
                    () -> assertThat(commentRepository.findAllById(List.of(3L, 4L))).isEmpty());
        }

        @Test
        void 대댓글을_삭제하는_경우_대댓글만_삭제된다() {
            // when
            commentService.deleteComment(3L);

            // then
            Assertions.assertAll(
                    () -> assertThat(commentRepository.findById(3L).isPresent()).isFalse(),
                    () ->
                            assertThat(commentRepository.findAllById(List.of(1L, 4L)).size())
                                    .isEqualTo(2));
        }

        @Test
        void 댓글이_존재하지_않는_경우_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(999L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 댓글_작성자가_아닌_경우_예외가_발생한다() {
            // given
            Member member = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(member);

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(1L))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CommentErrorCode.NOT_MY_COMMENT.getMessage());
        }
    }

    @Nested
    class 나의_댓글_목록을_조회할_때 {

        @BeforeEach
        void setUp() {
            Member member1 =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));
            Member member2 =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO));

            member1.updateProfile(
                    "testNickName1",
                    "testClokeyId1",
                    "testProfileImageUrl1",
                    "testProfileBackImageUrl1",
                    "testBio1",
                    Visibility.PUBLIC);
            member2.updateProfile(
                    "testNickName2",
                    "testClokeyId2",
                    "testProfileImageUrl2",
                    "testProfileBackImageUrl2",
                    "testBio2",
                    Visibility.PUBLIC);

            memberRepository.saveAll(List.of(member1, member2));
            given(memberUtil.getCurrentMember()).willReturn(member1);

            HistoryType historyType = HistoryType.createHistoryType("testType");
            historyTypeRepository.save(historyType);

            History history1 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 1), "testContent1", member1, historyType);
            History history2 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 2), "testContent2", member1, historyType);
            History history3 =
                    History.createHistory(
                            LocalDate.of(2025, 1, 3), "testContent3", member1, historyType);
            historyRepository.saveAll(List.of(history1, history2, history3));

            HistoryImage historyImage1_1 =
                    HistoryImage.createHistoryImage("testHistoryImageURl1_1", history1);
            HistoryImage historyImage1_2 =
                    HistoryImage.createHistoryImage("testHistoryImageURl1_2", history1);

            HistoryImage historyImage2_1 =
                    HistoryImage.createHistoryImage("testHistoryImageURl2_1", history2);
            HistoryImage historyImage2_2 =
                    HistoryImage.createHistoryImage("testHistoryImageURl2_2", history2);

            HistoryImage historyImage3_1 =
                    HistoryImage.createHistoryImage("testHistoryImageURl3_1", history3);
            HistoryImage historyImage3_2 =
                    HistoryImage.createHistoryImage("testHistoryImageURl3_2", history3);
            historyImageRepository.saveAll(
                    List.of(
                            historyImage1_1,
                            historyImage1_2,
                            historyImage2_1,
                            historyImage2_2,
                            historyImage3_1,
                            historyImage3_2));

            Comment comment1 = Comment.createParentComment("testContent1", member1, history1);
            Comment comment2 = Comment.createParentComment("testContent2", member1, history1);

            Comment comment3 = Comment.createParentComment("testContent3", member1, history2);
            Comment comment4 = Comment.createReply("testContent3", member1, history2, comment3);
            commentRepository.saveAll(List.of(comment1, comment2, comment3, comment4));
        }

        @Test
        void 댓글을_작성한_기록에_대해서_기록의_정보와_모든_댓글을_반환한다() {
            // when
            SliceResponse<MyCommentListResponse> response =
                    commentService.getMyComments(null, 3, SortDirection.ASC);

            Assertions.assertAll(
                    () ->
                            assertThat(response.content())
                                    .extracting(
                                            MyCommentListResponse::historyId,
                                            MyCommentListResponse::imageUrl,
                                            MyCommentListResponse::nickname,
                                            MyCommentListResponse::codiveId,
                                            MyCommentListResponse::historyDate,
                                            MyCommentListResponse::content,
                                            r ->
                                                    r.payloads().stream()
                                                            .map(p -> p.commentId())
                                                            .toList(),
                                            r ->
                                                    r.payloads().stream()
                                                            .map(p -> p.content())
                                                            .toList())
                                    .containsExactly(
                                            tuple(
                                                    1L,
                                                    "testHistoryImageURl1_1",
                                                    "testNickName1",
                                                    "testClokeyId1",
                                                    LocalDate.of(2025, 1, 1),
                                                    "testContent1",
                                                    List.of(1L, 2L),
                                                    List.of("testContent1", "testContent2")),
                                            tuple(
                                                    2L,
                                                    "testHistoryImageURl2_1",
                                                    "testNickName1",
                                                    "testClokeyId1",
                                                    LocalDate.of(2025, 1, 2),
                                                    "testContent2",
                                                    List.of(3L, 4L),
                                                    List.of("testContent3", "testContent3"))));
        }

        @Test
        void 정렬_조건이_ASC이면_historyId를_오름차순으로_조회한다() {
            // when
            SliceResponse<MyCommentListResponse> response =
                    commentService.getMyComments(null, 3, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("historyId").containsExactly(1L, 2L);
        }

        @Test
        void 정렬_조건이_DESC이면_historyId를_내림차순으로_조회한다() {
            // when
            SliceResponse<MyCommentListResponse> response =
                    commentService.getMyComments(null, 3, SortDirection.DESC);

            // then
            assertThat(response.content()).extracting("historyId").containsExactly(2L, 1L);
        }

        @Test
        void lastHistoryId를_입력하면_다음_history_부터_조회한다() {
            // when
            SliceResponse<MyCommentListResponse> response =
                    commentService.getMyComments(1L, 3, SortDirection.ASC);

            // then
            assertThat(response.content()).extracting("historyId").containsExactly(2L);
        }

        @Test
        void 댓글을_작성하지_않은_경우_빈_리스트를_조회한다() {
            // given
            Member member = memberRepository.findById(2L).orElseThrow();
            given(memberUtil.getCurrentMember()).willReturn(member);

            // when
            SliceResponse<MyCommentListResponse> response =
                    commentService.getMyComments(null, 3, SortDirection.ASC);

            // when & then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isZero(),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_반환한다() {
            // when
            SliceResponse<MyCommentListResponse> response =
                    commentService.getMyComments(null, 3, SortDirection.ASC);
            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(2),
                    () -> assertThat(response.isLast()).isTrue());
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_반환한다() {
            // when
            SliceResponse<MyCommentListResponse> response =
                    commentService.getMyComments(null, 1, SortDirection.ASC);

            // then
            Assertions.assertAll(
                    () -> assertThat(response.content().size()).isEqualTo(1),
                    () -> assertThat(response.isLast()).isFalse());
        }
    }
}
