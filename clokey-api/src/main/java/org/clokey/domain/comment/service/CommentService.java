package org.clokey.domain.comment.service;

import org.clokey.domain.comment.dto.request.CommentCreateRequest;
import org.clokey.domain.comment.dto.response.CommentCreateResponse;
import org.clokey.domain.comment.dto.response.CommentListResponse;
import org.clokey.domain.comment.dto.response.MyCommentListResponse;
import org.clokey.domain.comment.dto.response.ReplyListResponse;
import org.clokey.global.paging.SortDirection;
import org.clokey.response.SliceResponse;

public interface CommentService {

    CommentCreateResponse createComment(CommentCreateRequest request);

    CommentCreateResponse createReply(Long commentId, CommentCreateRequest request);

    SliceResponse<CommentListResponse> getHistoryComments(
            Long historyId, Long lastCommentId, int size, SortDirection direction);

    SliceResponse<ReplyListResponse> getCommentReplies(
            Long commentId, Long lastReplyId, int size, SortDirection direction);

    void deleteComment(Long commentId);

    SliceResponse<MyCommentListResponse> getMyComments(
            Long lastHistoryId, int size, SortDirection direction);
}
