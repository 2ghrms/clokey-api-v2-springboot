package org.clokey.domain.comment.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.request.ReplyCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.ReplyCreateResponse;
import org.clokey.domain.comment.exception.CommentErrorCode;
import org.clokey.domain.comment.service.CommentService;
import org.clokey.domain.history.exception.HistoryErrorCode;
import org.clokey.exception.BaseCustomException;
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
            ReplyCreateRequest request = new ReplyCreateRequest("testContent");
            ReplyCreateResponse response = new ReplyCreateResponse(1L);

            given(commentService.createReply(1L, request)).willReturn(response);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON201"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 리소스 생성됨"))
                    .andExpect(jsonPath("$.result.replyId").value(1));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 대댓글_내용이_null_또는_공백이면_예외가_발생한다(String content) throws Exception {
            // given
            ReplyCreateRequest request = new ReplyCreateRequest(content);

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.content").value("대댓글 내용을 비워둘 수 없습니다."));
        }

        @Test
        void 댓글이_존재하지_않는_경우_예외가_발생한다() throws Exception {
            // given
            ReplyCreateRequest request = new ReplyCreateRequest("testContent");
            given(commentService.createReply(1L, request))
                    .willThrow(new BaseCustomException(CommentErrorCode.COMMENT_NOT_FOUND));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1")
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
            ReplyCreateRequest request = new ReplyCreateRequest("testContent");
            given(commentService.createReply(1L, request))
                    .willThrow(new BaseCustomException(HistoryErrorCode.LIMITED_AUTHORITY));

            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            post("/comments/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            perform.andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("HISTORY_4031"))
                    .andExpect(jsonPath("$.message").value("기록에 대한 접근 권한이 없습니다."));
        }
    }
}
