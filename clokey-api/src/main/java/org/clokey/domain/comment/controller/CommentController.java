package org.clokey.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.MyCommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.domain.comment.service.CommentService;
import org.clokey.global.annotation.PageSize;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.BaseResponse;
import org.clokey.response.SliceResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "04. 댓글 API", description = "댓글 관련 API입니다.")
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(
            operationId = "Comment_createComment",
            summary = "댓글 작성",
            description = "새로운 댓글을 작성합니다.")
    public BaseResponse<CommentCreateResponse> createComment(
            @Valid @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = commentService.createComment(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/{commentId}/replies")
    @Operation(operationId = "Comment_createReply", summary = "대댓글 작성", description = "대댓글을 작성합니다.")
    public BaseResponse<CommentCreateResponse> createReply(
            @PathVariable Long commentId, @Valid @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = commentService.createReply(commentId, request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @GetMapping
    @Operation(operationId = "Comment_getComments", summary = "댓글 조회", description = "댓글을 조회합니다")
    public BaseResponse<SliceResponse<CommentListResponse>> getComments(
            @Parameter(description = "조회중인 기록의 ID") @RequestParam Long historyId,
            @Parameter(description = "이전 페이지의 마지막 댓글 ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastCommentId,
            @Parameter(description = "페이지당 조회할 댓글 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction) {
        SliceResponse<CommentListResponse> response =
                commentService.getHistoryComments(historyId, lastCommentId, size, direction);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @GetMapping("/{commentId}/replies")
    @Operation(operationId = "Comment_getReplies", summary = "대댓글 조회", description = "대댓글을 조회합니다")
    public BaseResponse<SliceResponse<ReplyListResponse>> getReplies(
            @PathVariable Long commentId,
            @Parameter(description = "이전 페이지의 마지막 대댓글 ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastReplyId,
            @Parameter(description = "페이지당 조회할 대댓글 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction) {
        SliceResponse<ReplyListResponse> response =
                commentService.getCommentReplies(commentId, lastReplyId, size, direction);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @DeleteMapping("/{commentId}")
    @Operation(
            operationId = "Comment_deleteComment",
            summary = "댓글 삭제 API",
            description = "댓글을 삭제합니다.")
    public BaseResponse<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping("/my-comments")
    @Operation(
            operationId = "Comment_getMyComments",
            summary = "내가 작성한 댓글 조회 API",
            description = "내가 작성한 댓글을 조회합니다.")
    public BaseResponse<SliceResponse<MyCommentListResponse>> getMyComments(
            @Parameter(description = "이전 페이지의 마지막 기록 ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long lastHistoryId,
            @Parameter(description = "페이지당 조회할 기록 수") @RequestParam @PageSize Integer size,
            @Parameter(description = "정렬 방향 (ASC: 오래된순, DESC: 최신순)")
                    @RequestParam(defaultValue = "DESC")
                    SortDirection direction) {
        SliceResponse<MyCommentListResponse> response =
                commentService.getMyComments(lastHistoryId, size, direction);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
}
