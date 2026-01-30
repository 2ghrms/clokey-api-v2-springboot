package org.clokey.domain.comment.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.MyCommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.service.CommentService;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CommentService commentService;

    @Nested
    class 댓글_작성_요청_시 {

        @Test
        void 유효한_요청이면_댓글을_생성하고_ID를_반환한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            CommentCreateResponse response = new CommentCreateResponse(1L);

            given(commentService.createComment(request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.commentId").value(1));
        }

        @Test
        void 기록_ID를_비워두면_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(null, "testContent");

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.historyId").value("기록 ID는 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 댓글_내용이_null_또는_공백이면_예외가_발생한다(String content) throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, content);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("댓글 내용을 비워둘 수 없습니다."));
        }

        @Test
        void 댓글의_길이가_100자를_넘어가면_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "t".repeat(101));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("댓글의 내용은 최대 100자까지 가능합니다."));
        }

        @Test
        void 기록이_존재하지_않는_경우_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(999L, "testContent");
            given(commentService.createComment(request))
                    .willThrow(new BaseCustomException(HistoryErrorCode.HISTORY_NOT_FOUND));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("HISTORY_4041"))
                    .andExpect(jsonPath("$.message").value("존재하지 않는 기록입니다."));
        }

        @Test
        void 내가_아닌_비공개_계정의_기록에_댓글을_작성하면_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            given(commentService.createComment(request))
                    .willThrow(new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("HISTORY_4031"))
                    .andExpect(jsonPath("$.message").value("기록에 대한 접근 권한이 없습니다."));
        }
    }

    @Nested
    class 대댓글_작성_요청_시 {

        @Test
        void 유효한_요청이면_대댓글을_생성하고_ID를_반환한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            CommentCreateResponse response = new CommentCreateResponse(1L);

            given(commentService.createReply(1L, request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1/replies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.commentId").value(1));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 대댓글_내용이_null_또는_공백이면_예외가_발생한다(String content) throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, content);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1/replies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("댓글 내용을 비워둘 수 없습니다."));
        }

        @Test
        void 대댓글_내용이_100를_넘어가면_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "t".repeat(101));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1/replies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("댓글의 내용은 최대 100자까지 가능합니다."));
        }

        @Test
        void 댓글이_존재하지_않는_경우_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            given(commentService.createReply(1L, request))
                    .willThrow(new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1/replies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMENT_4041"))
                    .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
        }

        @Test
        void 내가_아닌_비공개_계정의_기록의_댓글에_대댓글을_작성하면_예외가_발생한다() throws Exception {
            // given
            CommentCreateRequest request = new CommentCreateRequest(1L, "testContent");
            given(commentService.createReply(1L, request))
                    .willThrow(new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1/replies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("HISTORY_4031"))
                    .andExpect(jsonPath("$.message").value("기록에 대한 접근 권한이 없습니다."));
        }
    }

    @Nested
    class 기록의_댓글_목록_조회_요청_시 {

        @Test
        void 정렬_조건이_ASC이면_commentId를_오름차순으로_응답한다() throws Exception {
            // given
            List<CommentListResponse> historyComments =
                    List.of(
                            new CommentListResponse(
                                    1L,
                                    1L,
                                    "testNickName",
                                    "testProfile",
                                    "testContent1",
                                    false,
                                    0L,
                                    true),
                            new CommentListResponse(
                                    2L,
                                    1L,
                                    "testNickName",
                                    "testProfile",
                                    "testContent2",
                                    false,
                                    0L,
                                    true));

            given(commentService.getHistoryComments(1L, null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(historyComments, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments")
                                    .param("historyId", "1")
                                    .param("size", "2")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].commentId").value(1))
                    .andExpect(jsonPath("$.result.content[1].commentId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 정렬_조건이_DESC이면_commentId를_내림차순으로_응답한다() throws Exception {
            // given
            List<CommentListResponse> historyComments =
                    List.of(
                            new CommentListResponse(
                                    2L,
                                    1L,
                                    "testNickName",
                                    "testProfile",
                                    "testContent2",
                                    false,
                                    0L,
                                    true),
                            new CommentListResponse(
                                    1L,
                                    1L,
                                    "testNickName",
                                    "testProfile",
                                    "testContent1",
                                    false,
                                    0L,
                                    true));

            given(commentService.getHistoryComments(1L, null, 2, SortDirection.DESC))
                    .willReturn(new SliceResponse<>(historyComments, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments")
                                    .param("historyId", "1")
                                    .param("size", "2")
                                    .param("direction", "DESC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].commentId").value(2))
                    .andExpect(jsonPath("$.result.content[1].commentId").value(1))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_응답한다() throws Exception {
            // given
            List<CommentListResponse> historyComments =
                    List.of(
                            new CommentListResponse(
                                    2L,
                                    1L,
                                    "testNickName",
                                    "testProfile",
                                    "testContent2",
                                    false,
                                    0L,
                                    true));

            given(commentService.getHistoryComments(1L, null, 1, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(historyComments, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments")
                                    .param("historyId", "1")
                                    .param("size", "1")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].commentId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<CommentListResponse> historyComments =
                    List.of(
                            new CommentListResponse(
                                    1L,
                                    1L,
                                    "testNickName",
                                    "testProfile",
                                    "testContent1",
                                    false,
                                    0L,
                                    true),
                            new CommentListResponse(
                                    2L,
                                    1L,
                                    "testNickName",
                                    "testProfile",
                                    "testContent2",
                                    false,
                                    0L,
                                    true));

            given(commentService.getHistoryComments(1L, null, 1, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(historyComments, false));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments")
                                    .param("historyId", "1")
                                    .param("size", "1")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].commentId").value(1))
                    .andExpect(jsonPath("$.result.content[1].commentId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @Test
        void 기록에_댓글이_없는_경우_빈_리스트를_응답한다() throws Exception {
            // given
            List<CommentListResponse> historyComments = List.of();

            given(commentService.getHistoryComments(1L, null, 1, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(historyComments, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments")
                                    .param("historyId", "1")
                                    .param("size", "1")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content").isEmpty())
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments")
                                    .param("historyId", "1")
                                    .param("size", pageSize)
                                    .param("direction", "ASC"));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ASCC", "DESCC", "OLDEST", "NEWEST"})
        void 존재하지_않는_정렬_기준을_입력한_경우_예외가_발생한다(String sort) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments")
                                    .param("historyId", "1")
                                    .param("size", "1")
                                    .param("direction", sort));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }

    @Nested
    class 댓글의_대댓글_목록_조회_요청_시 {

        @Test
        void 정렬_조건이_ASC이면_replyId를_오름차순으로_응답한다() throws Exception {
            // given
            List<ReplyListResponse> commentReplies =
                    List.of(
                            new ReplyListResponse(
                                    1L, 1L, "testNickName", "testProfile", "testContent1", true),
                            new ReplyListResponse(
                                    2L, 1L, "testNickName", "testProfile", "testContent2", true));

            given(commentService.getCommentReplies(1L, null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(commentReplies, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/1/replies")
                                    .param("size", "2")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].replyId").value(1))
                    .andExpect(jsonPath("$.result.content[1].replyId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 정렬_조건이_DESC이면_replyId를_내림차순으로_응답한다() throws Exception {
            // given
            List<ReplyListResponse> commentReplies =
                    List.of(
                            new ReplyListResponse(
                                    2L, 1L, "testNickName", "testProfile", "testContent2", true),
                            new ReplyListResponse(
                                    1L, 1L, "testNickName", "testProfile", "testContent1", true));

            given(commentService.getCommentReplies(1L, null, 2, SortDirection.DESC))
                    .willReturn(new SliceResponse<>(commentReplies, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/1/replies")
                                    .param("size", "2")
                                    .param("direction", "DESC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].replyId").value(2))
                    .andExpect(jsonPath("$.result.content[1].replyId").value(1))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_응답한다() throws Exception {
            // given
            List<ReplyListResponse> commentReplies =
                    List.of(
                            new ReplyListResponse(
                                    2L, 1L, "testNickName", "testProfile", "testContent2", true));

            given(commentService.getCommentReplies(1L, null, 1, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(commentReplies, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/1/replies")
                                    .param("size", "1")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].replyId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<ReplyListResponse> commentsReplies =
                    List.of(
                            new ReplyListResponse(
                                    1L, 1L, "testNickName", "testProfile", "testContent1", true),
                            new ReplyListResponse(
                                    2L, 1L, "testNickName", "testProfile", "testContent2", true));

            given(commentService.getCommentReplies(1L, null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(commentsReplies, false));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/1/replies")
                                    .param("size", "2")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].replyId").value(1))
                    .andExpect(jsonPath("$.result.content[1].replyId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @Test
        void 댓글에_대댓글이_없는_경우_빈_리스트를_응답한다() throws Exception {
            // given
            List<ReplyListResponse> commentReplies = List.of();

            given(commentService.getCommentReplies(1L, null, 1, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(commentReplies, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/1/replies")
                                    .param("size", "1")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content").isEmpty())
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/1/replies")
                                    .param("size", pageSize)
                                    .param("direction", "ASC"));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ASCC", "DESCC", "OLDEST", "NEWEST"})
        void 존재하지_않는_정렬_기준을_입력한_경우_예외가_발생한다(String sort) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/1/replies").param("size", "1").param("direction", sort));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }

    @Nested
    class 댓글_삭제_요청_시 {

        @Test
        void 유효한_요청이면_댓글을_삭제하고_NO_CONTENT를_반환한다() throws Exception {
            willDoNothing().given(commentService).deleteComment(1L);

            // when & then
            ResultActions perform =
                    mockMvc.perform(delete("/comments/1").contentType(MediaType.APPLICATION_JSON));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 나의_댓글_목록_조회_요청_시 {

        @Test
        void 정렬_조건이_ASC이면_historyId를_오름차순으로_응답한다() throws Exception {
            // given
            List<MyCommentListResponse> myCommentListResponses =
                    List.of(
                            new MyCommentListResponse(
                                    1L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 1),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(1L, "testContent"),
                                            new MyCommentListResponse.Payload(2L, "testContent"))),
                            new MyCommentListResponse(
                                    2L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 2),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(3L, "testContent1"),
                                            new MyCommentListResponse.Payload(
                                                    4L, "testContent2"))));

            given(commentService.getMyComments(null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(myCommentListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/my-comments")
                                    .param("size", "2")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].historyId").value(1))
                    .andExpect(jsonPath("$.result.content[1].historyId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 정렬_조건이_DESC이면_historyId를_내림차순으로_응답한다() throws Exception {
            // given
            List<MyCommentListResponse> myCommentListResponses =
                    List.of(
                            new MyCommentListResponse(
                                    2L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 1),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(3L, "testContent"),
                                            new MyCommentListResponse.Payload(4L, "testContent"))),
                            new MyCommentListResponse(
                                    1L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 2),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(1L, "testContent1"),
                                            new MyCommentListResponse.Payload(
                                                    2L, "testContent2"))));

            given(commentService.getMyComments(null, 2, SortDirection.DESC))
                    .willReturn(new SliceResponse<>(myCommentListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/my-comments")
                                    .param("size", "2")
                                    .param("direction", "DESC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].historyId").value(2))
                    .andExpect(jsonPath("$.result.content[1].historyId").value(1))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지인_경우_isLast를_true로_응답한다() throws Exception {
            // given
            List<MyCommentListResponse> myCommentListResponses =
                    List.of(
                            new MyCommentListResponse(
                                    1L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 1),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(1L, "testContent"),
                                            new MyCommentListResponse.Payload(2L, "testContent"))),
                            new MyCommentListResponse(
                                    2L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 2),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(3L, "testContent1"),
                                            new MyCommentListResponse.Payload(
                                                    4L, "testContent2"))));

            given(commentService.getMyComments(null, 3, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(myCommentListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/my-comments")
                                    .param("size", "3")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].historyId").value(1))
                    .andExpect(jsonPath("$.result.content[1].historyId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<MyCommentListResponse> myCommentListResponses =
                    List.of(
                            new MyCommentListResponse(
                                    1L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 1),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(1L, "testContent"),
                                            new MyCommentListResponse.Payload(2L, "testContent"))),
                            new MyCommentListResponse(
                                    2L,
                                    "testImageUrl",
                                    "testNickname",
                                    LocalDate.of(2025, 1, 2),
                                    "testContent",
                                    List.of(
                                            new MyCommentListResponse.Payload(3L, "testContent1"),
                                            new MyCommentListResponse.Payload(
                                                    4L, "testContent2"))));

            given(commentService.getMyComments(null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(myCommentListResponses, false));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/my-comments")
                                    .param("size", "2")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content[0].historyId").value(1))
                    .andExpect(jsonPath("$.result.content[1].historyId").value(2))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @Test
        void 기록을_남긴_댓글이_없는_경우_빈_리스트를_응답한다() throws Exception {
            // given
            List<MyCommentListResponse> myCommentListResponses = List.of();

            given(commentService.getMyComments(null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<>(myCommentListResponses, true));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/my-comments")
                                    .param("size", "2")
                                    .param("direction", "ASC"));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.content").isEmpty())
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/my-comments")
                                    .param("lastHistoryId", "1")
                                    .param("size", pageSize)
                                    .param("direction", "ASC"));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }

        @ParameterizedTest
        @ValueSource(strings = {"ASCC", "DESCC", "OLDEST", "NEWEST"})
        void 존재하지_않는_정렬_기준을_입력한_경우_예외가_발생한다(String sort) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/comments/my-comments")
                                    .param("lastHistoryId", "1")
                                    .param("size", "1")
                                    .param("direction", sort));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."));
        }
    }
}
