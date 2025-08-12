package org.clokey.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.request.ReplyCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.ReplyCreateResponse;
import org.clokey.domain.comment.service.CommentService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "8. 댓글 API", description = "댓글 관련 API입니다.")
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다.")
    public BaseResponse<CommentCreateResponse> createComment(
            @Valid @RequestBody CommentCreateRequest request) {
        CommentCreateResponse response = commentService.createComment(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }

    @PostMapping("/{commentId}")
    @Operation(summary = "대댓글 작성", description = "대댓글을 작성합니다.")
    public BaseResponse<ReplyCreateResponse> createReply(
            @PathVariable Long commentId, @Valid @RequestBody ReplyCreateRequest request) {
        ReplyCreateResponse response = commentService.createReply(commentId, request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }
}
