package org.clokey.domain.comment.service;

import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.request.ReplyCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyCreateResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface CommentService {

    CommentCreateResponse createComment(CommentCreateRequest request);

    ReplyCreateResponse createReply(Long commentId, ReplyCreateRequest request);

    SliceResponse<CommentListResponse> getHistoryComments(
            Long historyId, Long lastCommentId, int size, SortDirection direction);

    SliceResponse<ReplyListResponse> getCommentReplies(
            Long commentId, Long lastReplyId, int size, SortDirection direction);

    void deleteComment(Long commentId);

    void deleteReply(Long commentId, Long replyId);
}
